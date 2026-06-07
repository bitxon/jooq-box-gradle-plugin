package dev.bitxon.gradle.jooq.extension.codegen

import org.gradle.api.provider.Property

abstract class GenerateExtension {
    abstract val pojos: Property<Boolean>
    abstract val immutablePojos: Property<Boolean>
    abstract val daos: Property<Boolean>
    abstract val interfaces: Property<Boolean>
    abstract val records: Property<Boolean>
    abstract val javaTimeTypes: Property<Boolean>
    abstract val springAnnotations: Property<Boolean>
    abstract val springDao: Property<Boolean>
    abstract val fluentSetters: Property<Boolean>
    abstract val globalObjectReferences: Property<Boolean>
}
