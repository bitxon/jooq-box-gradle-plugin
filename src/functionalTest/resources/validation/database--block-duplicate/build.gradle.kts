// Intentionally calls database.container { } twice.
// The second call should be rejected with GradleException — only one database block is allowed.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        container { type = "POSTGRES" }
        embedded { type = "H2" } // duplicate — not allowed
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
