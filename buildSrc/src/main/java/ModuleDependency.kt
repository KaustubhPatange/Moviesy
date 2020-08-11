import kotlin.reflect.full.memberProperties

const val NKO = ":app"

@Suppress("unused")
object ModuleDependency {
    const val APP = ":app"
    const val SHIMMER = ":app:shimmer"
    const val PURCHASE = ":app:purchase"
    const val COMMON = ":app:common"

    fun getAllModules() = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()
}