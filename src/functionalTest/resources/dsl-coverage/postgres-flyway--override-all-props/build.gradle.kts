
plugins {
    java
    id("dev.bitxon.jooq-box")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jooq:jooq-bom:3.19.31"))
    implementation("org.jooq:jooq")
    implementation("org.flywaydb:flyway-database-postgresql:11.14.1")
    runtimeOnly("org.postgresql:postgresql:42.7.10")
}

jooq {
    database {
        container {
            type = "POSTGRES"
            image = "postgres:16-alpine"
            databaseName = "overridedb"
            username = "overrideuser"
            password = "overridepass"
        }
    }
    migration {
        flyway {
            locations = listOf("filesystem:${project.projectDir}/src/main/resources/db/migration")
            defaultSchema = "custom_schema" //Flyway auto-creates the schema (default createSchemas=true) and sets search_path, so CREATE TABLE users (...) without a schema prefix ends up in custom_schema. jOOQ codegen then introspects custom_schema and finds the tables there.
            schemas = listOf("custom_schema")
            table = "custom_schema_history"
            properties = mapOf("outOfOrder" to "false")
        }
    }
    codegen {
        logging = "WARN"
        onError = "LOG"
        generator {
            name = "org.jooq.codegen.JavaGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "custom_schema"
                includes = "users|addresses" // "preferences" table is not included
                excludes = "custom_schema_history"
                forcedTypes {
                    forcedType {
                        // by default jOOQ generates Long for BIGINT; override to BigInteger
                        name = "DECIMAL_INTEGER"
                        includeExpression = ".*\\.points_amount"
                        includeTypes = "BIGINT"
                    }
                }
            }
            generate {
                pojos = true
                immutablePojos = false
                daos = true
                interfaces = false
                records = true
                javaTimeTypes = false // jOOQ 3.13+ defaults to true (LocalDate for DATE); override to java.sql.Date
                springAnnotations = false
                springDao = false
                fluentSetters = false
                globalObjectReferences = true
            }
            strategy {
                name = "org.jooq.codegen.DefaultGeneratorStrategy"
            }
            target {
                packageName = "com.example.jooq"
                directory = "build/custom-generated/jooq"
            }
        }
    }
}
