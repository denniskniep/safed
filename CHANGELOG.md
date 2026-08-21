# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

## [1.1.0] - 2026-08-21

### Added

- Machine-readable `Evidence` (status/type/value) replacing free-text evidence strings across scan results and reports
- `TitleVerification` - compares page title between baseline and scan
- `UrlVerification` now compares individual URL parts (scheme, host, port, path segments, query params, fragment) instead of the whole URL string, tolerating parts known to vary between the two baseline logins
- `CookieVerification` now actually diffs cookies present on baseline vs. scan (previously a stub that always reported OK)
- `LineDiffVerification` and `WordDiffVerification` (line-based and word-based text diffing), replacing `DiffVerification`; detection now also checks that only previously-seen-as-unstable lines/words were removed, cutting false positives
- Example OIDC app: fragment response mode flow (`fragmentflow` profile, port 8086) for testing implicit flow via URL fragment

### Changed

- `UrlAndStatusCodeVerification` split into `StatusCodeVerification` (status code only) and `UrlVerification` (URL only)
- Screenshot resolution raised from 320x240 to 640x480
- Selenium 4.41.0 → 4.47.0, Chromium 146 → 151.0.7922.169

## [1.0.9] - 2026-03-31

### Changed

- Import all certs from pem bundle
- More details for privacy error
- Fixed nested error message

## [1.0.8] - 2026-03-31

### Added

- Use Selenium Actions (click on button, enter text etc.) to initiate the sign-in

### Changed

- If first or second baseline scan returns status code >=400 than mark assessment as failed

## [1.0.7] - 2026-03-26

### Added

- Support SAML Request via Redirect

### Changed

- Default to responseMode fragement in oidc implicit and hybrid flow
- ScannerResult is marked as FAILED, if Assessment has errors
- Lift Chromium to 146.0.7680.153

## [1.0.6] - 2026-03-17

### Added

- Preflight connection check before opening website 

### Changed

- Removed Banner in CLI
- Compressing Screenshots
- List all scanners in failed assessment report
- enhance mTLS scanner reporting for socket level cert errors
- Lift Chromium to 146.0.7680.71

## [1.0.5] - 2026-03-15

### Added

- Support ValidRedirectUrls

## [1.0.4] - 2026-03-15

### Added

- Refactored Exception handling and reporting

## [1.0.3] - 2026-03-14

### Added

- Take Screenshots
- Dynamically adopt Verification Strategy
- change dns resolution via config

### Changed

- Improved Error Handling
- Refactored Bidi Network request capturing

## [1.0.1] - 2026-03-10

### Added

- Initial Release