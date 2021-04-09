plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.KAPT)
    kotlin(GradlePluginId.ANDROID_EXTENSIONS_KTX)
    id(GradlePluginId.DAGGER_HILT)
}

android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

    buildFeatures.viewBinding = true

    defaultConfig {
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(LibraryDependency.KOTLIN_STDLIB)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.CORE_KTX)

    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.OKHTTP_LOGGING_INTERCEPTOR)

    implementation(LibraryDependency.LIFECYCLE_VIEWMODEL)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.LIFECYCLE_LIVEDATA)
    implementation(LibraryDependency.LIFECYCLE_RUNTIME)

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
    kapt(LibraryDependency.HILT_VIEWODEL_COMPILER)
}
