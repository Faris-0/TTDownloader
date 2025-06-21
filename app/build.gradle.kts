plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.yuuna.ttdownloader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yuuna.ttdownloader"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.sdp.android)
}