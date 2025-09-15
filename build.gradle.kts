// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    if (GradleVersion.current() >= GradleVersion.version("8.0")){
        id("com.android.library") version "8.2.2" apply false
        id("org.jetbrains.kotlin.android") version "1.9.0" apply false
        id("com.android.application") version "8.2.2" apply false
    } else if (GradleVersion.current() >= GradleVersion.version("7.0")){
        id("com.android.library") version "7.3.1" apply false
        id("com.android.application") version "7.3.1" apply false
        id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    }

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
