import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.angeldevtech.gol"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.angeldevtech.gol"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val keyStoreFile = project.rootProject.file("secrets.properties")
        val properties = Properties()
        properties.load(keyStoreFile.inputStream())
        val apiBaseUrl = properties.getProperty("API_BASE_URL") ?: ""
        val imgBaseUrl = properties.getProperty("IMG_BASE_URL") ?: ""

        buildConfigField(
            type = "String",
            name = "API_BASE_URL",
            value = apiBaseUrl
        )
        buildConfigField(
            type = "String",
            name = "IMG_BASE_URL",
            value = imgBaseUrl
        )
    }

    buildTypes {
        create("staging") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    composeCompiler{
        reportsDestination = file("build/output/compose_reports")
        metricsDestination = file("build/output/compose_metrics")
    }
    packaging{
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // UI
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material.icons.extended)

    // Ktor for HTTP
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Kotlinx serialization
    implementation(libs.kotlinx.serialization.json)

    // Lifecycle (to use rememberCoroutineScope safely)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Image
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Video Player
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer.hls)

    // Jsoup
    implementation(libs.jsoup)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
}