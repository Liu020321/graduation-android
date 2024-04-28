plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.lht.graduation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lht.graduation"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("mysql:mysql-connector-java:8.0.27")
    implementation("com.google.android.material:material:1.4.0")

//    implementation("mysql:mysql-connector-java:5.1.49")


}