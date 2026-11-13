plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Temporary product-test switch. Set -PallowCleartext=false when HTTPS is available.
val allowCleartext = providers.gradleProperty("allowCleartext")
    .orElse("true")
    .get()
    .toBooleanStrictOrNull() ?: true

android {
    namespace = "com.cscareerguide.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cscareerguide.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("boolean", "ALLOW_CLEARTEXT", allowCleartext.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val debugApiBaseUrl = providers.gradleProperty("apiBaseUrl")
                .orElse(providers.environmentVariable("API_BASE_URL"))
                .getOrElse("")
            buildConfigField("String", "API_BASE_URL", "\"${debugApiBaseUrl.replace("\"", "\\\"")}\"")
        }
        release {
            val releaseApiBaseUrl = providers.gradleProperty("releaseApiBaseUrl")
                .orElse(providers.environmentVariable("RELEASE_API_BASE_URL"))
                .getOrElse("")
            require(
                releaseApiBaseUrl.isBlank() ||
                    releaseApiBaseUrl.startsWith("https://") ||
                    allowCleartext,
            ) {
                "releaseApiBaseUrl must use HTTPS."
            }
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${releaseApiBaseUrl.replace("\"", "\\\"")}\"",
            )
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.security.crypto)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
