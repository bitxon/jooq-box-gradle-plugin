// Intentionally omits database.container.type.
// The plugin should reject this at task execution with a clear GradleException.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        container { } // type is intentionally not set
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
