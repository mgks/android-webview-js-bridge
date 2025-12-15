# Android Library Template 📦

A Golden Template for creating and publishing Android Libraries (Kotlin) with automatic JitPack and GitHub Packages support.

## 🚀 How to use this template

### 1. Clone & Rename
1.  Click **"Use this template"** on GitHub to create a new repo (e.g., `android-biometric-gate`).
2.  Clone your new repo.

### 2. Configure Identity
Open `gradle.properties` and edit the library details:

```bash
LIB_ARTIFACT_ID=biometric-gate
LIB_VERSION=1.0.0
LIB_NAME=Android Biometric Gate
LIB_DESCRIPTION=Secure biometric auth...
LIB_URL=https://github.com/mgks/android-biometric-gate
```

### 3. Refactor Package
1.  Open the project in Android Studio.
2.  Go to `library/src/main/java/dev/mgks/swv/placeholder`.
3.  Right-click `placeholder` -> **Refactor** -> **Rename**.
4.  Rename it to your library name (e.g., `biometric`).
5.  Android Studio will update all package references.

### 4. Write Code
*   Add your library code in the `library` module.
*   Add permissions/manifest entries in `library/src/main/AndroidManifest.xml`.
*   Use the `app` module to test your library (Edit `app/src/main/java/.../MainActivity.kt`).

### 5. Publish
1.  Push your changes.
2.  Create a **Release** on GitHub (tag `v1.0.0`).
3.  The Action will auto-publish to GitHub Packages.
4.  JitPack will auto-pickup the release.