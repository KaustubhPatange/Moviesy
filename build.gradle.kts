buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(GradleDependency.GRADLE_BUILD_TOOLS)
        classpath(GradleDependency.KOTLIN_PLUGIN)
        classpath(GradleDependency.DAGGER_HILT)
        classpath(GradleDependency.MAVEN_GRADLE)
        classpath(GradleDependency.GOOGLE_SERVICE)
        classpath(GradleDependency.CRASHLYTICS)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
        mavenLocal()
        mavenCentral()
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}