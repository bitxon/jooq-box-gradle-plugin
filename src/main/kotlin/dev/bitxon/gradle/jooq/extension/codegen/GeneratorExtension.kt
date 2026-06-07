package dev.bitxon.gradle.jooq.extension.codegen

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class GeneratorExtension @Inject constructor(objects: ObjectFactory) {
    abstract val name: Property<String>
    val database: DatabaseSchemaExtension = objects.newInstance(DatabaseSchemaExtension::class.java)
    val generate: GenerateExtension = objects.newInstance(GenerateExtension::class.java)
    val strategy: StrategyExtension = objects.newInstance(StrategyExtension::class.java)
    val target: TargetExtension = objects.newInstance(TargetExtension::class.java)

    fun database(action: Action<in DatabaseSchemaExtension>) = action.execute(database)
    fun generate(action: Action<in GenerateExtension>) = action.execute(generate)
    fun strategy(action: Action<in StrategyExtension>) = action.execute(strategy)
    fun target(action: Action<in TargetExtension>) = action.execute(target)
}
