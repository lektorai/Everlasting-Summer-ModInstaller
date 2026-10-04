# Everlasting Summer 1.6 — in-game mod installer

This project patches an existing `Everlasting Summer_1.6.apk`.

## What it changes

- Adds an **Установить мод** button to the existing in-game Mods screen.
- Opens Android's system file picker.
- Imports a `.zip` mod directly into the game's external files directory.
- Accepts archives whose contents are already rooted for Android 1.6, and also strips a leading `game/` directory from PC-style archives.
- Prevents Zip Slip (`../`) paths.
- Restarts the game after a successful import so Ren'Py scans the new files.

The original game already expects Android 1.6 mods under:
`/Android/data/su.sovietgames.everlasting_summer/files/`.

## Build

Put the original APK in the repository root as:
`Everlasting Summer_1.6.apk`

Then run the GitHub Actions workflow **Build patched APK**.

The workflow:
1. downloads Ren'Py 7.0.0 SDK;
2. compiles `game/mod_installer.rpy`;
3. decodes the APK with apktool;
4. injects the compiled `.rpyc` and the Android picker activity;
5. rebuilds and signs the APK with a temporary test key;
6. uploads the patched APK as a workflow artifact.

The signed APK is a new build and will not update an existing copy signed with another key; uninstall the old build first or use the same signing key.
