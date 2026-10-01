# JetWatch Privacy Policy

Effective date: 1 October 2026

JetWatch is a flight tracker. It shows live aircraft on a map and flight schedules from public flight-data services. This policy describes what the app stores on your phone and what it sends off the phone.

JetWatch does not have user accounts. It does not ask for your name, email, phone number, or a password.

## Data stored on your phone

JetWatch keeps this data on the device only:

- Flights you follow, including the flight number, callsign, airline, status, origin, destination, times, and gate when those details are available.
- The last aircraft positions loaded for the map, so the map can still show something when the network fails.
- Flight details already loaded, so a flight page can open offline.
- Whether you allowed notifications.

That data is not uploaded to a JetWatch server. There is no JetWatch server. Unfollowing a flight removes it from the saved list. Uninstalling JetWatch, or clearing the app's storage in Android settings, deletes the rest. Steps are in [How to delete your data](delete-account.md).

## Data sent to other services

To show flights, the app sends requests to two services. Those requests are not tied to an account, and JetWatch does not attach your name or a user id.

- **OpenSky Network.** The app sends the map area currently on screen so OpenSky can return aircraft in that area. That area is the map you are looking at. It is not your GPS location.
- **AeroDataBox (RapidAPI).** When you open a flight or search, the app sends the flight number, callsign, airline code, or airport you asked about, so AeroDataBox can return the schedule.

Those services have their own privacy policies. JetWatch does not control what they store.

## Location

JetWatch does not request or collect your device location. The map opens over Nairobi and then follows the area you pan to. The app does not read GPS.

## Notifications

If you allow notifications and follow a flight, JetWatch checks that flight on the phone about every 15 minutes and can notify you when it is delayed, departs, or lands. The check uses the same AeroDataBox request described above. Notifications are created on the device. JetWatch does not use Firebase or another push service.

## What JetWatch does not do

- It does not create an account.
- It does not sell data.
- It does not show ads.
- It does not use analytics or advertising identifiers.
- It does not collect precise location.

## Children

JetWatch is not directed at children, and it does not knowingly collect personal information from children.

## Changes

If this policy changes, the updated version will replace this page and the effective date above will change.

## Contact

Questions about this policy: open an issue at [github.com/Diana-Kieru/JetWatch](https://github.com/Diana-Kieru/JetWatch/issues).
