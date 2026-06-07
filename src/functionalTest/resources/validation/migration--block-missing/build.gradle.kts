// Intentionally omits the migration { } block entirely.
// The plugin should reject this in afterEvaluate — flyway { }, liquibase { }, or sql { } is required.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        embedded { type = "H2" }
    }
    // migration block is intentionally missing
    codegen {
        generator {
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
