allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
        mavenCentral()
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
