package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ContainerDefaultsTest {

    @ParameterizedTest(name = "{0} has a defaults profile")
    @EnumSource(DatabaseContainerType::class)
    fun `all types have a defaults profile`(type: DatabaseContainerType) {
        assertThatCode { ContainerDefaults[type] }.doesNotThrowAnyException()
    }
}
