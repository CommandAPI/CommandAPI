#!/bin/bash

# Exit on error
set -e

# Output directory
DEST_DIR=$(pwd)

cd ..
version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# Declare associative array: [module_path]=custom_name
declare -A MODULES_TO_COPY=(
  ["commandapi-platforms/commandapi-spigot/commandapi-spigot-plugin"]="CommandAPI-$version-Spigot.jar"
  ["commandapi-platforms/commandapi-paper/commandapi-paper-plugin"]="CommandAPI-$version-Paper.jar"
  ["commandapi-platforms/commandapi-bukkit/commandapi-bukkit-networking-plugin"]="CommandAPI-$version-Networking-Plugin.jar"
  ["commandapi-platforms/commandapi-velocity/commandapi-velocity-plugin"]="CommandAPI-$version-Velocity.jar"
)

# Iterate over the array
for module in "${!MODULES_TO_COPY[@]}"; do
    custom_name="${MODULES_TO_COPY[$module]}"
    target_dir="$module/target"

    # Find main artifact (ignore sources/javadoc)
    artifact=$(find "$target_dir" -maxdepth 1 -type f \
        \( -name "*.jar" -o -name "*.war" \) \
        ! -name "*-sources.jar" ! -name "*-javadoc.jar" | head -n 1)

    if [[ -n "$artifact" ]]; then
        cp "$artifact" "$DEST_DIR/$custom_name"
        echo "Copied $artifact → $DEST_DIR/$custom_name"
    else
        echo "Warning: No artifact found in $target_dir"
    fi
done

echo "Done. Artifacts are in $DEST_DIR"
