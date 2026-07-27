# ProGuard configuration for sample app
-keep class com.paymentology.dxp.issuerpay.sample.** { *; }
-keepclassmembers class com.paymentology.dxp.issuerpay.sample.** { *; }

# Keep all UI components
-keep class com.paymentology.dxp.issuerpay.ui.** { *; }
