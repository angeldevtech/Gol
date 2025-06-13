# CI/CD Setup Guide

This document explains how to use the GitHub Actions workflow for building and releasing the Gol Android app.

## Overview

The CI/CD pipeline automatically:

1. Builds a release APK when you create a new tag
2. Signs the APK with your keystore
3. Creates a GitHub release with the APK
4. Generates a changelog based on commits
5. Links the release to the README.md files

## Prerequisites

Before you can use this workflow, you need to set up the following GitHub secrets:

### Required Secrets

| Secret Name | Description |
|-------------|-------------|
| `KEYSTORE_BASE64` | Your keystore file (main-keystore.jks) encoded as base64 |
| `KEYSTORE_PASSWORD` | The password for your keystore |
| `KEY_ALIAS` | The alias of the key in your keystore |
| `KEY_PASSWORD` | The password for your key |
| `API_BASE_URL` | The base URL for your API (used in secrets.properties) |
| `IMG_BASE_URL` | The base URL for images (used in secrets.properties) |

## How to Create a New Release

1. Make sure all your changes are committed and pushed to the main branch
2. Create and push a new tag with the version number:

```bash
# For version 1.0.0
git tag v1.0.0
git push origin v1.0.0
```

3. The workflow will automatically start when you push the tag
4. Once completed, a new release will be created on GitHub with:
   - The APK file named "Gol v1.0.0.apk"
   - A changelog generated from your commits
   - Links to your README files

## How to Encode Your Keystore as Base64

To add your keystore as a GitHub secret, you need to encode it as base64:

### On Windows

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\main-keystore.jks")) | Set-Clipboard
```

### On macOS/Linux

```bash
base64 -i path/to/main-keystore.jks | pbcopy
```

Then paste the copied base64 string as the value for the `KEYSTORE_BASE64` secret in your GitHub repository settings.

## Troubleshooting

- If the build fails, check the workflow logs for errors
- Ensure all required secrets are set correctly
- Verify that your keystore is valid and the passwords are correct

## Customizing the Workflow

If you need to customize the workflow, edit the `.github/workflows/android-release.yml` file. You can modify:

- The trigger conditions (e.g., to build on specific branches)
- The build parameters
- The release format and content

## Version Naming Convention

The workflow extracts the version number from the tag name. For example:

- Tag `v1.0.0` → Version `1.0.0`
- Tag `v2.1.0` → Version `2.1.0`

The version code is calculated as: `MAJOR * 10000 + MINOR * 100 + PATCH`

For example, version 1.2.3 would have version code 10203.