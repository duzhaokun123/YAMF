plugins {
    id("com.android.library")
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 31
        targetSdk = 34

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
    annotationProcessor("dev.rikka.tools.refine:annotation-processor:4.3.0")
    compileOnly("dev.rikka.tools.refine:annotation:4.3.0")
    compileOnly("androidx.annotation:annotation:1.7.1")
    compileOnly("dev.rikka.hidden:stub:4.2.0")
}
