# resolve-snapshots.py
#
# Usage:
#   python resolve-snapshots.py
#
# Description:
#   Resolves snapshot versions in the root pom.xml file. To have
#   a snapshot resolve, add a <spigot.version.blah> entry, give
#   it a version with -SNAPSHOT, and then run this script. This
#   script will update the pom.xml file accordingly. This script
#   will also remove all comments, but I CBA to fix that right
#   now.

import re
import sys
import time
import argparse
import urllib.request
import urllib.error
import xml.etree.ElementTree as ET
from pathlib import Path

SPIGOT_SNAP = "https://repo.codemc.io/repository/nms/"
PAPER_SNAP  = "https://repo.papermc.io/repository/maven-public"

def coord_for_property(prop_name: str):
    if prop_name.startswith("spigot.version."):
        return ("org.spigotmc", "spigot-api", SPIGOT_SNAP)
    if prop_name.startswith("paper.version."):
        return ("io.papermc.paper", "paper-api", PAPER_SNAP)
    return None

def fetch_text(url: str, timeout=8, retries=3, verbose=False) -> str:
    last_err = None
    for attempt in range(1, retries + 1):
        if verbose:
            print(f"[http] GET {url} (attempt {attempt}/{retries})", flush=True)
        try:
            req = urllib.request.Request(
                url,
                headers={"User-Agent": "snapshot-resolver/1.0 (+https://example.invalid)"}
            )
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return resp.read().decode("utf-8")
        except (urllib.error.HTTPError, urllib.error.URLError, TimeoutError) as e:
            last_err = e
            if verbose:
                print(f"[http] Error: {e}", flush=True)
            # small backoff
            time.sleep(0.8 * attempt)
    raise RuntimeError(f"Failed to fetch {url}: {last_err}")

def latest_snapshot_value(repo_base: str, group: str, artifact: str, snapshot_version: str, verbose=False) -> str:
    meta_url = "/".join([
        repo_base.rstrip("/"),
        group.replace(".", "/"),
        artifact,
        snapshot_version,
        "maven-metadata.xml"
    ])
    xml = fetch_text(meta_url, verbose=verbose)
    md = ET.fromstring(xml)

    svs = md.findall("./versioning/snapshotVersions/snapshotVersion")
    pom_value = None
    any_value = None
    for sv in svs:
        ext = sv.findtext("extension", "")
        val = sv.findtext("value")
        if val:
            any_value = val
            if ext == "pom":
                pom_value = val
    if pom_value:
        return pom_value
    if any_value:
        return any_value

    snap = md.find("./versioning/snapshot")
    if snap is not None:
        ts = snap.findtext("timestamp")
        bn = snap.findtext("buildNumber")
        if ts and bn:
            base = snapshot_version.replace("-SNAPSHOT", "")
            return f"{base}-{ts}-{bn}"
    raise RuntimeError("No snapshot value found in metadata")

def resolve_properties(pom_path: Path, verbose=False):
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    ET.register_namespace("", "http://maven.apache.org/POM/4.0.0")
    tree = ET.parse(pom_path)
    root = tree.getroot()

    props = root.find("m:properties", ns)
    if props is None:
        print("No <properties> found.")
        return

    changes = []
    for child in list(props):
        tag = re.sub(r"^\{.*\}", "", child.tag)  # strip ns
        coord = coord_for_property(tag)
        if not coord:
            continue

        current = (child.text or "").strip()
        if not current.endswith("-SNAPSHOT"):
            if verbose:
                print(f"[skip] {tag}: already pinned: {current}", flush=True)
            continue

        group, artifact, repo = coord
        print(f"[resolving] {tag}: {current} -> …", flush=True)
        try:
            resolved = latest_snapshot_value(repo, group, artifact, current, verbose=verbose)
            if resolved and resolved != current:
                print(f"[resolved]  {tag}: {resolved}", flush=True)
                changes.append((tag, current, resolved))
                child.text = resolved
            else:
                print(f"[no-change] {tag}: {current}", flush=True)
        except Exception as e:
            print(f"[WARN] {tag}: {current} -> could not resolve: {e}", flush=True)

    if not changes:
        print("No snapshot properties needed updates.")
        return

    backup = pom_path.with_suffix(".xml.bak")
    pom_path.rename(backup)
    tree.write(pom_path, encoding="utf-8", xml_declaration=True)

    print(f"\nUpdated {pom_path.name}. Backup: {backup.name}")
    for tag, old, new in changes:
        print(f" - {tag}: {old} -> {new}")

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("pom", nargs="?", default="pom.xml")
    ap.add_argument("-v", "--verbose", action="store_true")
    args = ap.parse_args()
    resolve_properties(Path(args.pom), verbose=args.verbose)

if __name__ == "__main__":
    main()