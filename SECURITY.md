# Security Policy

Thank you for taking the time to report a potential security issue. We take security seriously and appreciate responsible disclosure.

## Reporting a vulnerability

If you believe you have found a security vulnerability in this repository, please report it to us privately so we can investigate and remediate before public disclosure.

Preferred contact methods (in order):

1. Create a private Security Advisory via GitHub (recommended):
   - Go to this repository's [Security](https://github.com/CommandAPI/CommandAPI/security) tab, and click "Report a vulnerability".
   - Follow the prompts to provide details, affected versions, and reproduction steps.
   - *See also [Privately reporting a security vulnerability](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability#privately-reporting-a-security-vulnerability)*
2. Email: security@commandapi.dev

When reporting, please include:

- A clear, concise summary of the issue.
- Steps to reproduce the issue (ideally a minimal example).
- The impact of the issue and a suggested fix if you have one.
- Affected versions/commit hashes and environment details (OS, browser, runtime versions).
- Any exploit or proof-of-concept code (if available).

Do not post proof-of-concept exploit code publicly until the issue is resolved or we’ve agreed to disclosure.

## Scope

### In-scope

- Code in this repository (all branches unless otherwise noted).
- Configuration files stored in the repository.
- Any GitHub Actions workflows defined in this repository.

### Out-of-scope

- Third-party dependencies (please report upstream to the dependency maintainer as well).
- Services and infrastructure not managed in this repository (e.g., production servers, hosted APIs) unless explicitly documented as part of this project.
- Physical attacks, social engineering of third parties, or attacks against other users.

If you are unsure whether something is in-scope, please report it privately and we will clarify.

## Safe testing guidelines

- Do not access, modify or exfiltrate personal data you are not explicitly authorized to access.
- Do not perform destructive testing on production systems (e.g., data deletion, denial-of-service) without prior written permission.
- Limit testing to data you control, and avoid creating a public nuisance.

## Disclosure policy and timeline

We follow a typical responsible disclosure process. Our goals are to acknowledge and triage reports quickly and to resolve verified issues in a timely manner.

- **Acknowledgement:** within 5 business days of receipt.
- **Triage & initial response:** within 10 business days with an estimated remediation plan or request for more information.
- **Fix & release:** timelines vary depending on severity and complexity. We will coordinate with the reporter about disclosure.

If you need faster acknowledgement or have not heard from us within these windows, follow up using the designated contact method.
