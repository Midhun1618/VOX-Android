plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.voxcom.vox"
    val weatherApiKey: String = project.findProperty("WEATHER_API_KEY") as String? ?: ""
    compileSdk = 35

    defaultConfig {
        applicationId = "com.voxcom.vox"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.1.1"

        buildConfigField(
            "String",
            "WEATHER_API_KEY",
            "\"$weatherApiKey\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
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

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // ✅ Credential Manager (modern Google Sign-In)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

}
