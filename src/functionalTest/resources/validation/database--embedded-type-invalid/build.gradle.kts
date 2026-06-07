// Intentionally sets database.embedded.type to an unsupported value.
// The plugin should reject this in afterEvaluate with a clear GradleException.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        embedded { type = "fakedb" } // not a supported embedded type
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
