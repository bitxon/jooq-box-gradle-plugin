package dev.bitxon.gradle.jooq.extension.database

import org.gradle.api.provider.Property

abstract class DatabaseEmbeddedExtension {
    abstract val type: Property<String>
}
