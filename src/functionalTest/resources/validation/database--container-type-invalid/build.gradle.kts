// Intentionally sets database.container.type to an unsupported value.
// The plugin should reject this in afterEvaluate with a clear GradleException.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        container { type = "fakedb" } // not a supported container type
    }
    migration {
        flyway { }
    }
    codegen {
        generator {
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
