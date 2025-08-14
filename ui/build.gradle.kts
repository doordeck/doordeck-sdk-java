plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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

    packaging {
        resources {
            excludes += "META-INF/atomicfu.kotlin_module"
        }
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
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // scan
    implementation("com.journeyapps:zxing-android-embedded:4.1.0") {
        isTransitive = false
    }
    implementation("com.google.zxing:core:3.4.0")

    // location
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.doordeck.headless.sdk:doordeck-sdk:${libs.versions.doordeck.sdk.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}