package dev.bitxon.gradle.jooq.extension.migration

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class MigrationExtension @Inject constructor(objects: ObjectFactory) {
    internal val flyway: MigrationFlywayExtension = objects.newInstance(MigrationFlywayExtension::class.java)
    internal val liquibase: MigrationLiquibaseExtension = objects.newInstance(MigrationLiquibaseExtension::class.java)
    internal val sql: MigrationSqlExtension = objects.newInstance(MigrationSqlExtension::class.java)
    internal abstract val kind: Property<MigrationKind>

    fun flyway(action: Action<in MigrationFlywayExtension>) {
        if (kind.isPresent) throw GradleException(
            "jooq: migration kind already set to '${kind.get()}'. Configure only one migration block."
        )
        kind.set(MigrationKind.FLYWAY)
        action.execute(flyway)
    }

    fun liquibase(action: Action<in MigrationLiquibaseExtension>) {
        if (kind.isPresent) throw GradleException(
            "jooq: migration kind already set to '${kind.get()}'. Configure only one migration block."
        )
        kind.set(MigrationKind.LIQUIBASE)
        action.execute(liquibase)
    }

    fun sql(action: Action<in MigrationSqlExtension>) {
        if (kind.isPresent) throw GradleException(
            "jooq: migration kind already set to '${kind.get()}'. Configure only one migration block."
        )
        kind.set(MigrationKind.SQL)
        action.execute(sql)
    }
}
