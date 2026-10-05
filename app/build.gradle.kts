plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.seanfarmer.catdog"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.seanfarmer.catdog"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    androidResources { noCompress += "tflite" }
}
dependencies {
    // Standalone LiteRT Interpreter runtime; no Play services or network at inference time.
    implementation("com.google.ai.edge.litert:litert:1.4.2")
    implementation("androidx.activity:activity-ktx:1.10.1")
    testImplementation("junit:junit:4.13.2")
}
