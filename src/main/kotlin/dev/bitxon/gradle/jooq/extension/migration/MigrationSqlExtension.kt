package dev.bitxon.gradle.jooq.extension.migration

import org.gradle.api.file.ConfigurableFileCollection
import javax.inject.Inject

abstract class MigrationSqlExtension @Inject constructor() {
    abstract val scripts: ConfigurableFileCollection
}
