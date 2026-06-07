package dev.bitxon.gradle.jooq.extension.codegen

import org.gradle.api.provider.Property

abstract class TargetExtension {
    abstract val packageName: Property<String>

    // Output directory. Relative paths resolve from the project root; absolute paths are used as-is.
    // Defaults to build/generated-sources/jooq when not set.
    abstract val directory: Property<String>
}
