object Plugin {

    object Android {
        private const val gradle_version = "7.0.2"
        const val gradle: String = "com.android.tools.build:gradle:$gradle_version"
    }

    object Kotlin {
        private const val kotlin_version = "1.5.30"
        const val gradle: String = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }

    object Dagger {
        private const val dagger_version = "2.38.1"
        const val gradle: String = "com.google.dagger:hilt-android-gradle-plugin:$dagger_version"
    }
}