plugins {
    id("com.android.library")
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 31
        lint.targetSdk = 35

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

    namespace = "com.mja.android_stub"
}

dependencies {
    annotationProcessor(libs.rikka.annotation.processor)
    compileOnly(libs.rikka.annotation)
    compileOnly(libs.androidx.annotation)
    compileOnly(libs.rikka.hidden.stub)
}
