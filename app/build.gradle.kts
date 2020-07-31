plugins {
    id(GradlePluginId.ANDROID_APPLICATION)
    kotlin(GradlePluginId.ANDROID_KTX)
    kotlin(GradlePluginId.ANDROID_EXTENSIONS_KTX)
    kotlin(GradlePluginId.KAPT)
    id(GradlePluginId.SAFE_ARGS)
    id(GradlePluginId.DAGGER_HILT)
}

android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

    viewBinding.isEnabled = true

    defaultConfig {
        applicationId = AndroidConfig.ID
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
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
    for (moduleId in ModuleDependency.getAllModules().filterNot { it == ModuleDependency.APP })
        implementation(project(moduleId))
    implementation(LibraryDependency.KOTLIN_STDLIB)
    implementation(LibraryDependency.APP_COMPAT)
    implementation(LibraryDependency.CORE_KTX)
    implementation(LibraryDependency.COROUTINES_CORE)
    implementation(LibraryDependency.COROUTINES_ANDROID)

    implementation(LibraryDependency.MEDIA_ROUTER)
    implementation(LibraryDependency.CAST_FRAMEWORK)
    implementation(LibraryDependency.ANDROID_LOCAL_CAST_SAMPLE)

    api(LibraryDependency.MATERIAL)
    api(LibraryDependency.GIRAFFE_PLAYER2)

    implementation(LibraryDependency.GSON)
    implementation(LibraryDependency.ANDROID_BROWSER)
    implementation(LibraryDependency.CONSTRAINT_LAYOUT)
    implementation(LibraryDependency.ANDROIDX_PREFERENCES)
    implementation(LibraryDependency.FB_SHIMMER)
    implementation(LibraryDependency.NAVIGATION_FRAGMENT)
    implementation(LibraryDependency.NAVIGATION_UI)
    implementation(LibraryDependency.ROOM_RUNTIME)
    implementation(LibraryDependency.ROOM_KTX)
    implementation(LibraryDependency.LOCALBROADCAST_MANAGER)
    implementation(LibraryDependency.LIFECYCLE_EXTENSIONS)
    implementation(LibraryDependency.LIFECYCLE_VIEWMODEL)
    implementation(LibraryDependency.LIFECYCLE_COMMON)
    implementation(LibraryDependency.LIFECYCLE_RUNTIME)
    implementation(LibraryDependency.LIFECYCLE_SAVEDSTATE)
    implementation(LibraryDependency.PAGING)
    implementation(LibraryDependency.RETROFIT) {
        exclude("okhttp")
    }
    implementation(LibraryDependency.OKHTTP)
    implementation(LibraryDependency.OKHTTP_LOGGING_INTERCEPTOR)
    implementation(LibraryDependency.RETROFIT_GSON_CONVERTER)
    implementation(LibraryDependency.RETROFIT_COROUTINES_ADAPTER)
    implementation(LibraryDependency.SWIPE_REFRESH_LAYOUT)
    implementation(LibraryDependency.GLIDE)
    implementation(LibraryDependency.RXANDROID)
    implementation(LibraryDependency.RXJAVA)
    implementation(LibraryDependency.RXBINDING)
    implementation(LibraryDependency.TOASTY)
    implementation(LibraryDependency.BLURRY)
    implementation(LibraryDependency.JSOUP)
    implementation(LibraryDependency.YOUTUBE_PLAYER)
    implementation(LibraryDependency.PHOTOVIEW)
    implementation(LibraryDependency.TORRENT_STREAM_ANDROID)
    implementation(LibraryDependency.CAFEBAR)
    implementation(LibraryDependency.HILT_ANDROID)
    implementation(LibraryDependency.HILT_VIEWODEL)

    kapt(LibraryDependency.ROOM_COMPILER_KAPT)
    kapt(LibraryDependency.GLIDE_COMPILER)
    kapt(LibraryDependency.HILT_COMPILER)
    kapt(LibraryDependency.HILT_VIEWODEL_COMPILER)

    debugImplementation(TestLibraryDependency.ANDROID_DEBUG_DB)

    annotationProcessor(LibraryDependency.GLIDE_COMPILER)
    testImplementation(TestLibraryDependency.JUNIT)
    androidTestImplementation(TestLibraryDependency.JUNIT_TEST_EXT)
    androidTestImplementation(TestLibraryDependency.ESPRESSO_CORE)
}
