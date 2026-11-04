# Drift lives in core, and the domain does not import it

The Flutter app has three roots. `lib/domain` holds money, entities, use cases, and repository interfaces, and it imports neither `core` nor `presentation`. `lib/core` holds Drift and the camera and bank adapters, implements those interfaces, and may import `domain`. `lib/presentation` holds the screens and the Riverpod providers that wire a core implementation to a use case. The app targets iOS and Android and opens on the portfolio, with no sign-in.

Putting Drift in a shared kernel that the domain imported would cycle `domain` and `core`. A fourth infrastructure root was the other option; the app keeps three.
