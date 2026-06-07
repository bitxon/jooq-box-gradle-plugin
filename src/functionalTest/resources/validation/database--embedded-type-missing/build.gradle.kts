// Intentionally omits database.embedded.type.
// The plugin should reject this at task execution with a clear GradleException.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        embedded { } // type is intentionally not set
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
