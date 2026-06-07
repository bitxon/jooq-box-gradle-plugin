package dev.bitxon.gradle.jooq.extension.codegen

import org.gradle.api.provider.Property

abstract class StrategyExtension {
    abstract val name: Property<String>
}
