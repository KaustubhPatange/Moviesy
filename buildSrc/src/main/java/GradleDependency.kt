private object GradlePluginVersion {
    const val KOTLIN = CoreVersion.KOTLIN
    const val ANDROID_GRADLE = "3.6.3"
    const val SAFE_ARGS = CoreVersion.JETPACK_NAVIGATION
}

object GradlePluginId {
    const val ANDROID_APPLICATION = "com.android.application"
    const val ANDROID_LIBRARY = "com.android.library"
    const val ANDROID_KTX = "android"
    const val ANDROID_EXTENSIONS_KTX = "android.extensions"
    const val KAPT = "kapt"
    const val SAFE_ARGS = "androidx.navigation.safeargs.kotlin"
    const val DAGGER_HILT = "dagger.hilt.android.plugin"
}

object GradleDependency {
    const val GRADLE_BUILD_TOOLS = "com.android.tools.build:gradle:${GradlePluginVersion.ANDROID_GRADLE}"
    const val KOTLIN_PLUGIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${GradlePluginVersion.KOTLIN}"
    const val SAFE_ARGS = "androidx.navigation:navigation-safe-args-gradle-plugin:${GradlePluginVersion.SAFE_ARGS}"
    const val HILT = "com.google.dagger:hilt-android-gradle-plugin:${CoreVersion.HILT}"
}