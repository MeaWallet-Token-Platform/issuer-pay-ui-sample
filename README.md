# Issuer Pay UI Sample

Sample Android app for demonstrating the `issuer-pay-ui` library.

Complete documentation: [Developer portal](https://developer.meawallet.com/mtp/overview)

## Audience

Use this project as a reference when integrating Issuer Pay UI into a client app or when reviewing 
the expected wiring around wallet registration, push handling, and digitization flows.

## Getting started

- Issuer Pay UI Compose integration
- Wallet registration flow
- Firebase push handling
- Sample digitization flows and card UI

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

