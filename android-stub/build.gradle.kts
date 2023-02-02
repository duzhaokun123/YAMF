plugins {
    id("com.android.library")
    id("dev.rikka.tools.refine") version "3.1.1"
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 31
        targetSdk = 33

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

    namespace = "io.github.duzhaokun123.android_stub"
}

dependencies {
    compileOnly("androidx.annotation:annotation:1.5.0")
    compileOnly("dev.rikka.hidden:stub:3.4.3")
}
