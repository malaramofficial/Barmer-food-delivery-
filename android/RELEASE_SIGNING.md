# Barmer Food Delivery — permanent release signing

This project uses a permanent release/upload keystore. The private keystore is intentionally **not stored in this public repository**.

## GitHub Actions secrets

Configure these repository secrets before running the Android Build workflow:

- `RELEASE_KEYSTORE_BASE64` — base64 contents of the permanent `.jks` file
- `RELEASE_STORE_PASSWORD` — keystore password
- `RELEASE_KEY_ALIAS` — key alias
- `RELEASE_KEY_PASSWORD` — key password

The workflow restores the keystore only inside the ephemeral runner, signs the release APK/AAB, verifies the signatures, uploads the artifacts, and deletes the restored keystore.

## Why this is required

Release builds are deliberately configured to **fail instead of producing an unsigned APK/AAB** when the release signing credentials are absent.

Do not commit the `.jks`/`.keystore` file or passwords to source control.

## Play Store

For Google Play, use the signed AAB and configure Play App Signing in Play Console. Google can then manage the app-signing key while this permanent key can serve as the upload key.

## Google Sign-In

After the release/upload certificate is finalized, register its public SHA-1/SHA-256 fingerprints with any provider that requires certificate registration. If Play App Signing is enabled, also register the Play app-signing certificate fingerprint where required; the Play-distributed certificate can differ from the local upload certificate.
