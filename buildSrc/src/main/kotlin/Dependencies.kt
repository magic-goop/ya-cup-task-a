object Dependencies {
    object AndroidX {
        private const val ktx_version = "1.3.2"
        const val coreKtx: String = "androidx.core:core-ktx:$ktx_version"

        private const val compat_version = "1.2.0"
        const val appCompat: String = "androidx.appcompat:appcompat:$compat_version"

//        private const val lifecycle_version = "2.4.0-alpha03"
        private const val lifecycle_version = "2.3.1"
        const val lifecycleKtx: String =
            "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
        const val viewModelKtx: String =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"

        private const val fragment_version = "1.3.6"
        const val fragmentKtx: String = "androidx.fragment:fragment-ktx:$fragment_version"

        private const val constrain_version = "2.1.1"
        const val constraintLayout: String =
            "androidx.constraintlayout:constraintlayout:$constrain_version"

        private const val navigation_version = "2.3.5"
        const val navigationFragmentKtx: String =
            "androidx.navigation:navigation-fragment-ktx:$navigation_version"
        const val navigationUiKtx: String =
            "androidx.navigation:navigation-ui-ktx:$navigation_version"
    }

    object Google {
        private const val material_version = "1.3.0"
        const val material: String = "com.google.android.material:material:$material_version"
    }

    object Timber {
        private const val timber_version = "5.0.1"
        const val timber: String = "com.jakewharton.timber:timber:$timber_version"
    }

    object Dagger {
        private const val hilt_version = "2.38.1"
        const val hilt: String = "com.google.dagger:hilt-android:$hilt_version"
        const val compiler = "com.google.dagger:hilt-compiler:$hilt_version"
    }

    object Kotlin {
        private const val coroutines_version = "1.5.2"
        const val coroutines: String =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
    }

    object Insetter {
        private const val insetter_version = "0.6.0"
        const val insetter: String = "dev.chrisbanes.insetter:insetter:$insetter_version"
    }

    object Tensorflow {
        private const val tensorflow_version = "0.2.0"
        const val tensorflow: String =
            "org.tensorflow:tensorflow-lite-task-audio:$tensorflow_version"
    }

    object Testing {
        private const val jUnit5_version = "5.3.2"
        private const val jUnitKt_version = "1.1.3"
        private const val mockk_version = "1.12.0"

        val jUnit5: String = "org.junit.jupiter:junit-jupiter-engine:$jUnit5_version"
        val jUnitKtx: String = "androidx.test.ext:junit-ktx:$jUnitKt_version"
        val mockk: String = "io.mockk:mockk:$mockk_version"
    }

}
