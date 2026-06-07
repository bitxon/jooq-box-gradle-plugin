// Intentionally calls migration.flyway { } and migration.liquibase { } together.
// The second call should be rejected with GradleException — only one migration block is allowed.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        embedded { type = "H2" }
    }
    migration {
        flyway { }
        liquibase { } // duplicate — not allowed
    }
    codegen {
        generator {
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
