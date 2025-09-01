import kotlin.properties.ReadOnlyProperty

fun gradlePropertyDelegate(): ReadOnlyProperty<Any?, String> =
    ReadOnlyProperty { _, property ->
        providers.gradleProperty(property.name).get()
    }

val projectName by gradlePropertyDelegate()

rootProject.name = projectName
