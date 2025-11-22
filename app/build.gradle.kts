import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.iamashad.foxtodo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.iamashad.foxtodo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_18)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // room db with ksp
    implementation("androidx.room:room-runtime:2.8.4")
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.material3)
    ksp("androidx.room:room-compiler:2.8.4")

    // navigation with compose
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // material icons
    implementation("androidx.compose.material:material-icons-core:1.7.8")

    // material-3
    implementation("androidx.compose.material3:material3:1.4.0")

    // hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-android-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-fragment:1.3.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.hilt:hilt-work:1.3.0")
    ksp("androidx.hilt:hilt-compiler:1.3.0")
    implementation("androidx.activity:activity-ktx:1.12.0")

    // google-fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.9.5")

    // work-manager
    implementation("androidx.work:work-runtime-ktx:2.11.0")

    // coroutines test (use runTest / TestDispatcher)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    // junit
    testImplementation("junit:junit:4.13.2")

    // mocking for Kotlin (MockK)
    testImplementation("io.mockk:mockk:1.14.6")

    // mockito Kotlin helper
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")

    // animated navigation
    implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}