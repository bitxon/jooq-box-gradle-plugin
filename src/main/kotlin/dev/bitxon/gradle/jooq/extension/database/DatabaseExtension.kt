package dev.bitxon.gradle.jooq.extension.database

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class DatabaseExtension @Inject constructor(objects: ObjectFactory) {
    internal val container: DatabaseContainerExtension = objects.newInstance(DatabaseContainerExtension::class.java)
    internal val embedded: DatabaseEmbeddedExtension   = objects.newInstance(DatabaseEmbeddedExtension::class.java)
    internal abstract val kind: Property<DatabaseKind>

    fun container(action: Action<in DatabaseContainerExtension>) {
        if (kind.isPresent) throw GradleException(
            "jooq: database kind already set to '${kind.get()}'. Configure only one database block."
        )
        kind.set(DatabaseKind.CONTAINER)
        action.execute(container)
    }

    fun embedded(action: Action<in DatabaseEmbeddedExtension>) {
        if (kind.isPresent) throw GradleException(
            "jooq: database kind already set to '${kind.get()}'. Configure only one database block."
        )
        kind.set(DatabaseKind.EMBEDDED)
        action.execute(embedded)
    }
}
