plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.campusvault"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.campusvault"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"),
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

    // Firebase BOM
    implementation(platform(
        "com.google.firebase:firebase-bom:33.0.0"))
    implementation(
        "com.google.firebase:firebase-auth")
    implementation(
        "com.google.firebase:firebase-firestore")
    implementation(
        "com.google.firebase:firebase-storage")

    // Google Sign-In
    implementation(
        "com.google.android.gms:play-services-auth:21.0.0")

    // AndroidX + UI
    implementation(
        "androidx.appcompat:appcompat:1.6.1")
    implementation(
        "com.google.android.material:material:1.11.0")
    implementation(
        "androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.activity)

    // RecyclerView and CardView
    implementation(
        "androidx.recyclerview:recyclerview:1.3.2")
    implementation(
        "androidx.cardview:cardview:1.0.0")

    // Glide for image loading
    implementation(
        "com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Testing — required to fix ExampleUnitTest
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(
        "androidx.test.ext:junit:1.1.5")
    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.5.1")

}