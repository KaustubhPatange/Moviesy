import kotlin.reflect.full.memberProperties

@Suppress("unused")
object ModuleDependency {
    const val APP = ":app"
    const val PURCHASE = ":app:purchase"
    const val COMMON = ":app:common"
    const val AFTER = ":app:after"
    const val SHEETS = ":app:sheets"
//    const val NAVIGATION = ":app:navigation"

    fun getAllModules() = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()
}