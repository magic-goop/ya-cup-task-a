plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Application.compileSdk

    defaultConfig {
        applicationId = Application.id

        versionCode = Application.versionCode
        versionName = Application.versionName

        targetSdk = Application.targetSdk
        minSdk = Application.minSdk

        vectorDrawables {
            useSupportLibrary = true
        }
        setProperty("archivesBaseName", "${applicationId}-v${versionName}(${versionCode})")
    }

    signingConfigs {
        getByName("debug") {
            storePassword = "123456"
            keyAlias = "TaskA"
            keyPassword = "123456"
            storeFile = file("debug_keystore")
        }
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            isMinifyEnabled = false
            isTestCoverageEnabled = true

            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        named("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        named("main") {
            java {
                srcDirs("src/main/kotlin")
            }
        }
        named("test") {
            java {
                srcDirs("src/test/kotlin")
            }
        }
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*.kotlin_module"
            excludes += "DebugProbesKt.bin"
        }
    }
}

dependencies {
    implementation(Dependencies.AndroidX.appCompat)
    implementation(Dependencies.AndroidX.constraintLayout)
    implementation(Dependencies.AndroidX.coreKtx)
    implementation(Dependencies.AndroidX.fragmentKtx)
    implementation(Dependencies.AndroidX.lifecycleKtx)
    implementation(Dependencies.AndroidX.navigationFragmentKtx)
    implementation(Dependencies.AndroidX.navigationUiKtx)
    implementation(Dependencies.AndroidX.viewModelKtx)

    implementation(Dependencies.Dagger.hilt)

    implementation(Dependencies.Google.material)

    implementation(Dependencies.Insetter.insetter)

    implementation(Dependencies.Kotlin.coroutines)

    implementation(Dependencies.Tensorflow.tensorflow)

    implementation(Dependencies.Timber.timber)

    kapt(Dependencies.Dagger.compiler)

    testImplementation(Dependencies.Testing.jUnit5)
    testImplementation(Dependencies.Testing.jUnitKtx)
    testImplementation(Dependencies.Testing.mockk)
}

kapt {
    correctErrorTypes = true
}
