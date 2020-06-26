import kotlin.reflect.full.memberProperties

@Suppress("unused")
object ModuleDependency {
    const val APP = ":app"
    const val SHIMMER = ":app:shimmer"

    fun getAllModules() = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()
}