package dev.bitxon.gradle.jooq.extension.codegen

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class ForcedTypeExtension {
    abstract val name: Property<String>
    abstract val userType: Property<String>
    abstract val includeExpression: Property<String>
    abstract val excludeExpression: Property<String>
    abstract val includeTypes: Property<String>
    abstract val excludeTypes: Property<String>
    abstract val converter: Property<String>
    abstract val binding: Property<String>
    abstract val nullability: Property<String>   // ALL | NOT_NULL | NULL
    abstract val objectType: Property<String>    // ALL | ATTRIBUTE | COLUMN | ELEMENT | PARAMETER | SEQUENCE
}

internal fun ForcedTypeExtension.encodeToString(): String = buildString {
    mapOf(
        "name" to name,
        "userType" to userType,
        "includeExpression" to includeExpression,
        "excludeExpression" to excludeExpression,
        "includeTypes" to includeTypes,
        "excludeTypes" to excludeTypes,
        "converter" to converter,
        "binding" to binding,
        "nullability" to nullability,
        "objectType" to objectType,
    ).forEach { (key, prop) -> prop.orNull?.let { appendLine("$key=$it") } }
}

class ForcedTypesScope(
    private val objects: ObjectFactory,
    private val list: MutableList<ForcedTypeExtension>,
) {
    fun forcedType(action: Action<ForcedTypeExtension>) {
        list.add(objects.newInstance(ForcedTypeExtension::class.java).also { action.execute(it) })
    }
}
