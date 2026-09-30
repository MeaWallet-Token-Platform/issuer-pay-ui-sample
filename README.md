# Issuer Pay UI Sample

Sample Android app for demonstrating the `issuer-pay-ui` library.

Complete documentation: [Developer portal](https://developer.meawallet.com/mtp/overview)

## Audience

Use this project as a reference when integrating Issuer Pay UI into a client app or when reviewing 
the expected wiring around wallet registration, push handling, and digitization flows.

## What's included

- Issuer Pay UI Compose integration
- Wallet registration flow
- Firebase push handling
- Sample digitization flows and card UI
- Application-level dependency injection (`sample/di`) for shared SDK services

## Integration copy path (recommended order)

1. **App initialization**
   - `SampleApp.kt`
   - `di/AppContainer.kt`, `di/AppContainerImpl.kt`
   - Initialize SDK once in application lifecycle, create app-scoped dependencies, start registration + card event subscriptions.
2. **Registration lifecycle**
   - `issuerpay/RegistrationCoordinator.kt`
   - `issuerpay/messaging/PushServiceInstanceManagerImpl.kt` (`PushServiceInstanceManager`)
   - Keep wallet registration state app-scoped, and provide push token access used by registration and token refresh flows.
   - Retry registration on connectivity/app resume.
3. **Push handling**
   - `issuerpay/messaging/MyFcmListenerService.kt`
   - `issuerpay/messaging/PushMessageCoordinator.kt`
   - `issuerpay/messaging/PushServiceInstanceManagerImpl.kt` (`PushServiceInstanceManager`) for FCM token acquisition and refresh propagation to SDK registration/update flows.
   - Keep FCM service thin and delegate all message parsing/routing to coordinator.
4. **Card update events**
   - `issuerpay/CardEventSubscriptions.kt`
   - Subscribe to SDK replenish/state-change callbacks and fan out updates through `CardEvents`.
5. **State management (MVI-style)**
   - `ui/viewmodel/CardListViewModel.kt`
   - `ui/viewmodel/SettingsViewModel.kt`
   - Handle SDK calls in ViewModels, expose typed UI state + one-off effects to screens.
6. **UI shell and screens**
   - `MainActivity.kt`
   - `ui/SampleAppScreen.kt`, `ui/CardListScreen.kt`, `ui/SettingsScreen.kt`, `ui/DigitizeFlowScreen.kt`
   - Keep activity/shell responsible for wiring and side effects; keep feature screens render-focused.

## Replace sample values before production

- `build.gradle.kts`: replace legacy `applicationId` and simulator SDK dependency.
- `settings.gradle.kts`: replace sample Nexus repository credentials.
- `src/main/google-services.json`: use your Firebase project configuration.
- `ui/DigitizeFlowScreen.kt` and related sample generators: replace demo card/cardholder test values.

## Production hardening checklist

- Remove/disable reset-and-kill sample behavior if not needed in production.
- Use your own signing config/keystore and register certificate fingerprints with Paymentology.
- Review optional UI customization hooks (`issuerpay/UiConfigurator.kt`) before enabling.

## Tested versions

| Component | Version |
| --- | --- |
| Java | 11 |
| Gradle | 9.3.0 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.3.10 |


## Getting started

1. Open the project in Android Studio or build it from the command line.
2. Review the `TODO` comments and replace sample-specific values with your own.
3. Update the Nexus repository credentials in `settings.gradle.kts`.
4. Replace the legacy `applicationId` in `build.gradle.kts` with your package name.
5. Replace the simulator SDK dependency with the company-specific MTP SDK build when ready.


## Provide configuration files

Firebase is enabled through `google-services.json`.

- Use a Firebase project dedicated to the sample app.
- Make sure the package name in the config matches the app `applicationId`.

## Add signing config

Add your signing configuration (usually ```keystore.jks``` file). Make sure you have shared the certificate's fingerprint with Paymentology.

## Notes

- This repository is intentionally sample-focused and not a production app template.
