
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
    implementation("org.liquibase:liquibase-core:5.0.2")
    runtimeOnly("com.mysql:mysql-connector-j:9.6.0")
}

jooq {
    database {
        container {
            type = "MYSQL"
            image = "mysql:8.4"
            databaseName = "overridedb"
            username = "overrideuser"
            password = "overridepass"
        }
    }
    migration {
        liquibase {
            changeLogFile = "src/main/resources/db/changelog/db.changelog-root.xml"
            defaultSchemaName = "overridedb"
            liquibaseSchemaName = "overridedb"
            databaseChangeLogTableName = "custom_changelog"
            databaseChangeLogLockTableName = "custom_changelog_lock"
            parameters = mapOf("outputDefaultSchema" to "false")
        }
    }
    codegen {
        logging = "WARN"
        onError = "LOG"
        generator {
            name = "org.jooq.codegen.JavaGenerator"
            database {
                name = "org.jooq.meta.mysql.MySQLDatabase"
                inputSchema = "overridedb"
                includes = "users|addresses" // "preferences" table is not included
                excludes = "custom_changelog|custom_changelog_lock"
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
