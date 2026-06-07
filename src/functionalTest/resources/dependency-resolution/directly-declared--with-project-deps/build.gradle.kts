plugins {
    java
    id("dev.bitxon.jooq-box")
}

repositories {
    mavenCentral()
}

dependencies {
    // Application dependencies
    // (none)

    // Plugin dependencies
    // Project deps only — no explicit tool versions declared.
    jooqCodegen(project(":codegen-extra"))
    jooqMigration(project(":migration-scripts"))
    jooqDatabase(project(":database-extra"))
}

jooq {
    database {
        container {
            type = "POSTGRES"
        }
    }
    migration {
        flyway {}
    }
    codegen {
        generator {
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
