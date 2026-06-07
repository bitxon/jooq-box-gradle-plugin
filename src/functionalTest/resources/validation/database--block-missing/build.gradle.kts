// Intentionally omits the database { } block entirely.
// The plugin should reject this in afterEvaluate — database.container { } or database.embedded { } is required.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    // database block is intentionally missing
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
