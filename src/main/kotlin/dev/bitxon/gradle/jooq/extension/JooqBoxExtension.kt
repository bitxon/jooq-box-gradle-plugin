package dev.bitxon.gradle.jooq.extension

import dev.bitxon.gradle.jooq.extension.codegen.CodegenExtension
import dev.bitxon.gradle.jooq.extension.database.DatabaseExtension
import dev.bitxon.gradle.jooq.extension.migration.MigrationExtension
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class JooqBoxExtension @Inject constructor(objects: ObjectFactory) {
    val database: DatabaseExtension = objects.newInstance(DatabaseExtension::class.java)
    val migration: MigrationExtension = objects.newInstance(MigrationExtension::class.java)
    val codegen: CodegenExtension = objects.newInstance(CodegenExtension::class.java)

    fun database(action: Action<in DatabaseExtension>) = action.execute(database)
    fun migration(action: Action<in MigrationExtension>) = action.execute(migration)
    fun codegen(action: Action<in CodegenExtension>) = action.execute(codegen)
}
