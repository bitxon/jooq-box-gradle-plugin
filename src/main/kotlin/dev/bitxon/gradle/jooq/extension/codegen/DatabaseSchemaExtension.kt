package dev.bitxon.gradle.jooq.extension.codegen

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class DatabaseSchemaExtension @Inject constructor(private val objects: ObjectFactory) {
    abstract val name: Property<String>
    abstract val inputSchema: Property<String>
    abstract val includes: Property<String>
    abstract val excludes: Property<String>

    internal val forcedTypeList: MutableList<ForcedTypeExtension> = mutableListOf()

    fun forcedTypes(action: Action<ForcedTypesScope>) =
        action.execute(ForcedTypesScope(objects, forcedTypeList))
}
