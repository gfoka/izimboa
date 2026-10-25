plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Numéro de version : celui du run GitHub Actions en CI (toujours croissant pour Google Play), 1 en local.
val ciBuildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

android {
    namespace = "com.izimboa.livreur"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.izimboa.livreur"
        minSdk = 26          // Android 8 : Tecno / Infinix / Itel depuis 2018
        targetSdk = 36
        versionCode = ciBuildNumber
        versionName = "0.1.$ciBuildNumber"

        buildConfigField("String", "API_URL", "\"https://api.izimboa.com\"")
        buildConfigField("String", "AUTH_URL", "\"https://auth.izimboa.com/realms/izimboa\"")
        buildConfigField("String", "CLIENT_ID", "\"izimboa-mobile\"")
        buildConfigField("String", "REDIRECT_URI", "\"izimboa://livreur/callback\"")
        manifestPlaceholders["appAuthRedirectScheme"] = "izimboa"
    }

    // Signature de publication : fournie par la CI (secrets GitHub), jamais dans le dépôt.
    val keystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.play.services.location)
    implementation(libs.appauth)
    implementation(libs.okhttp)
    implementation(libs.okhttp.coroutines)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    // Captures d'écran des écrans sur la JVM (sans téléphone) : ./gradlew :app:testDebugUnitTest -Droborazzi.test.record=true
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    systemProperty("roborazzi.test.record", System.getProperty("roborazzi.test.record") ?: "false")
    // Image Android de Robolectric déjà téléchargée (poste sans accès direct au dépôt) : ROBOLECTRIC_DEPS_DIR.
    System.getenv("ROBOLECTRIC_DEPS_DIR")?.let {
        systemProperty("robolectric.offline", "true")
        systemProperty("robolectric.dependency.dir", it)
    }
}
