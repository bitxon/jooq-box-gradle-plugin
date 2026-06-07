package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class EmbeddedDefaultsTest {

    @ParameterizedTest(name = "{0} has a defaults profile")
    @EnumSource(DatabaseEmbeddedType::class)
    fun `all types have a defaults profile`(type: DatabaseEmbeddedType) {
        assertThatCode { EmbeddedDefaults[type] }.doesNotThrowAnyException()
    }
}
