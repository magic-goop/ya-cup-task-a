import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Plugin.Android.gradle)
        classpath(Plugin.Kotlin.gradle)
        classpath(Plugin.Dagger.gradle)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
    tasks.withType<Test> {
        testLogging {
            setExceptionFormat("full")
            events = setOf(
                TestLogEvent.STARTED,
                TestLogEvent.SKIPPED,
                TestLogEvent.PASSED,
                TestLogEvent.FAILED
            )
            showStandardStreams = true
        }
        useJUnitPlatform()
    }
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xallow-result-return-type",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlinx.coroutines.FlowPreview",
                "-Xopt-in=kotlinx.coroutines.InternalCoroutinesApi"
            )
            jvmTarget = "${JavaVersion.VERSION_1_8}"
        }
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
