# JetWatch

Android flight tracker. The map shows live aircraft, and flight search fills in the schedule OpenSky does not have.

## What it does

- **Live map.** Plane icons rotate with heading and refresh every 12 seconds, only for the area on screen. The map opens over Nairobi.
- **Details.** Tap a plane for the callsign, altitude, speed, and heading. JetWatch asks AeroDataBox for the flight number, airline, origin, destination, gate, and status. OpenSky callsigns are matched to AeroDataBox callsign and flight-number search.
- **Search.** Flight number (`KQ100`), airport (`NBO` or `HKJK`), airline (`KQ`), or an airport name.
- **Saved flights.** Follow a flight and keep it on the device.
- **Alerts.** WorkManager checks followed flights about every 15 minutes and notifies you when one is delayed, departs, or lands.
- **Offline.** The last aircraft positions and flight details are stored on the device and shown when the network fails.

Firebase Cloud Messaging is not wired up. Alerts are local notifications from WorkManager.

## Stack

The flight tracker lives in the `:shared` Kotlin Multiplatform module, used by the Android app and the desktop app. Compose UI, OpenSky and AeroDataBox clients (Ktor), and the on-device JSON cache are shared. Android adds the MapLibre map, the splash screen, and WorkManager alerts. Desktop shows the same Kenya airspace as a list. There is no iOS target yet.

## API keys

Keys stay in `local.properties` (already gitignored):

```properties
AERODATABOX_API_KEY=your_rapidapi_key
OPENSKY_CLIENT_ID=
OPENSKY_CLIENT_SECRET=
```

- **AeroDataBox** (RapidAPI) is required for schedules, search, and alerts. The header sent is `X-RapidAPI-Key`.
- **OpenSky** works anonymously at a low limit. A client id and secret from an OpenSky account raise that limit. The app sends them as an OAuth client-credentials token when both are set.
- Rebuild after changing `local.properties`. Values are copied into `BuildConfig` and are not committed.

The map only requests the visible bounding box, waits 12 seconds between polls, and skips the request when the view is wider than 30 degrees. AeroDataBox responses are cached on the device.

## Run

Open the project in Android Studio and run the `app` configuration, or:

```shell
./gradlew :app:assembleDebug :shared:jvmTest
```

Desktop:

```shell
./gradlew :shared:run
```

## Privacy, terms, and deleting data

JetWatch has no user account. Saved flights and the last map positions stay on the phone.

- [Privacy policy](docs/privacy-policy.md)
- [Terms of use](docs/terms-of-use.md)
- [How to delete your data](docs/delete-account.md)

## Play Store checklist

Not published yet. Before an internal testing track:

- Privacy policy, terms, and data deletion are in `docs/`. The Play Console links have to be public. This GitHub repo is private, so those blob URLs will not load for a reviewer until the repo or those pages are public.
- Data safety: device storage, and notifications the user opts into by following a flight. No precise location.
- Declare the notification permission (`POST_NOTIFICATIONS`) and the internet permission.
- Replace the launcher icon if you want a store-specific asset. `ic_stat_plane` is the notification icon.
- Turn on release minify only after a release ProGuard pass. It is off for now.
- Exercise follow, unfollow, airplane mode (cached planes and cached flight details), and a notification tap, which opens that flight.
