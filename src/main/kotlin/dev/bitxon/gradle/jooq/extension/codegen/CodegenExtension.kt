package dev.bitxon.gradle.jooq.extension.codegen

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class CodegenExtension @Inject constructor(objects: ObjectFactory) {
    val generator: GeneratorExtension = objects.newInstance(GeneratorExtension::class.java)

    abstract val configFile: RegularFileProperty
    abstract val logging: Property<String>
    abstract val onError: Property<String>

    fun generator(action: Action<in GeneratorExtension>) = action.execute(generator)
}
