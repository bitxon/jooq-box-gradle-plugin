plugins {
    java
    id("dev.bitxon.jooq-box")
}

repositories {
    mavenCentral()
}

dependencies {
    // Application dependencies
    // User manages application dependency versions via platform BOMs in standard configurations
    implementation(platform("org.jooq:jooq-bom:3.18.31"))
    implementation(platform("io.dropwizard.flywaydb:flyway-bom:11.11.1"))
    implementation("org.postgresql:postgresql:42.6.2") // no BOM available for the PostgreSQL JDBC driver

    // Plugin dependencies
    // (none — defaultDependencies provides all versions)
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
