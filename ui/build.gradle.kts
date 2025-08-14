plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

group = "com.github.doordeck"

val fallbackNfcUriHost = "doordeck.link"
val fallbackNfcUriScheme = "https"

val nfcScheme = providers.gradleProperty("nfcUri.scheme").orElse(fallbackNfcUriScheme)
val nfcHost = providers.gradleProperty("nfcUri.host").orElse(fallbackNfcUriHost)

android {
    namespace = "com.github.doordeck.ui"

    defaultConfig {
        minSdk = 26
        compileSdk = 35
        buildToolsVersion = "35.0.0"
        vectorDrawables.useSupportLibrary = true

        resValue("string", "nfc_uri_host", nfcHost.get())
        resValue("string", "nfc_uri_scheme", nfcScheme.get())
        manifestPlaceholders["nfc_uri_host"] = nfcHost.get()
        manifestPlaceholders["nfc_uri_scheme"] = nfcScheme.get()
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL_API", "\"https://api.staging.doordeck.com\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            buildConfigField("String", "BASE_URL_API", "\"https://api.doordeck.com\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // scan
    implementation(libs.journeyapps.zxing)

    // location
    implementation(libs.play.auth)
    implementation(libs.play.location)

    implementation(libs.doordeck.headless.sdk)
    implementation(libs.kotlinx.serialization.json)
}