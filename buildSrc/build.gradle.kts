import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

object GradlePluginVersion {
    const val KOTLIN = "1.4.0"
    const val ANDROID_GRADLE = "3.6.3"
    const val SAFE_ARGS = "2.3.0-rc01"
    const val HILT = "2.28-alpha"
}

object GradlePluginId {
    const val ANDROID_KTX = "android"
    const val ANDROID_EXTENSIONS_KTX = "android.extensions"
    const val KAPT = "kapt"
    const val SAFE_ARGS = "androidx.navigation.safeargs.kotlin"
    const val DAGGER_HILT = "dagger.hilt.android.plugin"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${GradlePluginVersion.KOTLIN}")
    implementation("com.android.tools.build:gradle:${GradlePluginVersion.ANDROID_GRADLE}")
    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:${GradlePluginVersion.SAFE_ARGS}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${GradlePluginVersion.HILT}")
    implementation("com.github.dcendents:android-maven-gradle-plugin:2.1")
}

repositories {
    google()
    jcenter()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
    mavenCentral()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}