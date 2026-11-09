# Living Energy 4K — v20

Crystal-wave live wallpaper for Android TV with color palettes, a crystal-wave launcher icon and TV banner, and an APK updater.

## One-time release setup

Create the Actions repository secret `LIVING_ENERGY_SIGNING` using the entire private JSON signing bundle supplied separately. Never commit this bundle, keystore, or passwords. Keep a private backup: every future version must use this same key.

Run **Build Android TV APK** on `v20-auto-updates`. With the secret configured the workflow builds, lints, signs and verifies a release, then publishes `LivingEnergy.apk` and `update.json` under tag `v20`. Without the secret it produces a clearly labeled test-only debug build and does not publish an update.

The first stable release may require reinstalling a previous debug-signed app. Record your wallpaper settings before removing the old app. Subsequent stable updates preserve application data.

## Future versions

Increment both `versionCode` and `versionName`. Push the completed version on `v20-auto-updates` (or `main` after merging). Releases are immutable in this workflow: an existing tag is never overwritten. A release is only published after successful lint/build/signature validation.

## Updater

- Checks on application launch (12-hour throttle) and a persisted daily network-constrained JobScheduler task. Android chooses the exact background execution time.
- Automatically downloads a newer APK to private app storage; the user confirms installation through Android.
- Requires HTTPS, repository-scoped APK links, SHA-256 and size validation, matching package/version and the same signing certificate as the installed app.
- A pending update appears in the application and, if notifications are permitted, as a notification. Unknown-source installation permission may be required once.
- The automatic update switch cancels scheduled checks. Manual checks remain available.
- No GitHub credentials are embedded in the app. The release repository must remain publicly accessible.

## Validation

CI runs `assembleDebug` and `lintDebug`, plus `assembleRelease` and `apksigner verify` when permanent signing is configured. Actual Android TV installation and update prompts require device testing.
