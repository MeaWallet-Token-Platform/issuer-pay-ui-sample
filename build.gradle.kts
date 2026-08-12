import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.paymentology.dxp.issuerpay.sample"
    compileSdk = 36

    defaultConfig {
        // TODO: Replace this legacy applicationId with your own package name before public release.
        applicationId = "com.meawallet.mtp.test.app" // legacy id to use existing Firebase project

        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    lint {
        targetSdk = 36
    }

    configurations.all {
        // TODO: Exclude simulator dependencies to use company specific MTP-SDK dependency
//        exclude(group = "com.meawallet", module = "mtp-mea-simulator-test")
        resolutionStrategy {
            // TODO: Add company specific MTP-SDK dependency
            force("com.meawallet:mtp-mea-simulator-test:4.3.13-debug")
        }
    }
}

dependencies {
    // Issuer Pay UI
    implementation("com.paymentology:dxp-issuer-pay-ui-compose:0.3.0")

    // TODO: Add company specific MTP-SDK dependency and remove the simulator artifact
    implementation("com.meawallet:mtp-mea-simulator-test:4.3.13-debug")

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect) // necessary dependency issuer pay ui if nexus not used

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.ui.graphics)
    debugImplementation(libs.bundles.compose.tooling)

    // Material 3
    implementation(libs.compose.material3)
    implementation(libs.material.icons.extended)

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.constraintlayout)

    // Material Design
    implementation(libs.material3)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.bundles.testing.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.bundles.testing.compose)
    debugImplementation(libs.androidx.test.compose.manifest)

//    implementation(libs.mtp.sdk)

    // Firebase Messaging
    implementation(libs.firebase.messaging)
}
