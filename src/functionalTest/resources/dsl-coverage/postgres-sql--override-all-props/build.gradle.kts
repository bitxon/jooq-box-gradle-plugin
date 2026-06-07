
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
        sql {
            scripts.from(
                "${project.projectDir}/src/main/resources/db/migration/V1__create_users.sql",
                "${project.projectDir}/src/main/resources/db/migration/V2__create_addresses.sql",
                "${project.projectDir}/src/main/resources/db/migration/V3__create_preferences.sql"
            )
        }
    }
    codegen {
        logging = "WARN"
        onError = "LOG"
        generator {
            name = "org.jooq.codegen.JavaGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                includes = "users|addresses" // "preferences" table is not included
                excludes = ""
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
