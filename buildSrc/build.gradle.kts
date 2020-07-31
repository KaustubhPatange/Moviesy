plugins {
    java
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    /*kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.ANDROID_EXTENSIONS_KTX)
    kotlin(GradlePluginId.KAPT)*/
    //id(GradlePluginId.DAGGER_HILT)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

object PluginsVersion {

}

subprojects {
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
        implementation(LibraryDependency.APP_COMPAT)
        /*implementation(LibraryDependency.APP_COMPAT)
        implementation(LibraryDependency.CORE_KTX)

        implementation(LibraryDependency.OKHTTP)
        implementation(LibraryDependency.OKHTTP_LOGGING_INTERCEPTOR)
        implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
        implementation(LibraryDependency.RETROFIT_COROUTINES_ADAPTER)

        implementation(LibraryDependency.LIFECYCLE_EXTENSIONS)
        implementation(LibraryDependency.LIFECYCLE_VIEWMODEL)
        implementation(LibraryDependency.LIFECYCLE_COMMON)
        implementation(LibraryDependency.LIFECYCLE_LIVEDATA)
        implementation(LibraryDependency.LIFECYCLE_RUNTIME)
        implementation(LibraryDependency.LIFECYCLE_SAVEDSTATE)

        implementation(LibraryDependency.HILT_ANDROID)
        implementation(LibraryDependency.HILT_VIEWODEL)

        implementation(LibraryDependency.RETROFIT) {
            exclude("okhttp")
        }
        implementation(LibraryDependency.OKHTTP)
        implementation(LibraryDependency.OKHTTP_LOGGING_INTERCEPTOR)
        implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
        implementation(LibraryDependency.RETROFIT_COROUTINES_ADAPTER)

        kapt(LibraryDependency.HILT_COMPILER)
        kapt(LibraryDependency.HILT_VIEWODEL_COMPILER)*/
    }
}

repositories {
    google()
    jcenter()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
    mavenCentral()
}
