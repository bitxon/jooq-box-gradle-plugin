# jooq-box-gradle-plugin

**jOOQ Box** ties together everything [jOOQ](https://www.jooq.org/) codegen needs:
- Spins up a real database via [Testcontainers](https://testcontainers.com/)
- Runs your migrations ([Flyway](https://flywaydb.org/), [Liquibase](https://www.liquibase.org/), plain SQL scripts)
- Generates type-safe Java, Kotlin, or Scala code

No local database to maintain. No Docker Compose in CI. No provisioning required.

---

## Quickstart

```kotlin
// build.gradle.kts
plugins {
    id("dev.bitxon.jooq-box") version "0.0.0-SNAPSHOT"
}

jooq {
    database {
        container { type = "POSTGRES" }
    }
    migration {
        flyway { } // uses src/main/resources/db/migration by default
    }
    codegen {
        generator {
            database {
                inputSchema = "public"
                excludes = "flyway_schema_history"
            }
            target { packageName = "com.example.jooq" }
        }
    }
}
```

```bash
./gradlew generateJooq
```

Generated sources land in `build/generated-sources/jooq` and are wired into `compileJava` automatically.

---

## Dependency configurations

Three configurations map to the three codegen phases. Defaults apply unless you declare a dependency — overriding one does not affect the others.

| Configuration | Used for | Default |
|---|---|---|
| `jooqDatabase` | JDBC driver | Driver for the configured database type |
| `jooqMigration` | Migration tool | Flyway or Liquibase at the plugin's bundled version |
| `jooqCodegen` | jOOQ codegen | `org.jooq:jooq-codegen` at the plugin's bundled version |

```kotlin
dependencies {
    jooqDatabase("org.postgresql:postgresql:42.7.11")
    jooqMigration("org.flywaydb:flyway-core:11.14.1")
    jooqMigration("org.flywaydb:flyway-database-postgresql:11.14.1")
    jooqCodegen("org.jooq:jooq-codegen:3.19.33")
}
```

> **Testcontainers** is bundled and its version is pinned. Customize the Docker image via `database.container.image`.

---

## DSL reference

Full examples:
- [postgres + flyway](src/functionalTest/resources/dsl-coverage/postgres-flyway--override-all-props/build.gradle.kts)
- [mysql + liquibase](src/functionalTest/resources/dsl-coverage/mysql-liquibase--override-all-props/build.gradle.kts)
- [postgres + sql](src/functionalTest/resources/dsl-coverage/postgres-sql--override-all-props/build.gradle.kts)
- [h2 + flyway|configFile](src/functionalTest/resources/dsl-coverage/h2-flyway--config-file/build.gradle.kts)

<details>
<summary><b><code>database {}</code></b></summary>

```kotlin
database {
    // pick exactly one:

    container {                               // Docker via Testcontainers
        type         = "POSTGRES"             // required | POSTGRES, MYSQL, MARIADB
        image        = "postgres:16"          // optional | auto-selected by type
        databaseName = "test"                 // optional | default: test
        username     = "test"                 // optional | default: test
        password     = "test"                 // optional | default: test
    }

    // or

    embedded {                                // no Docker needed
        type = "H2"                           // required | H2, SQLITE, HSQLDB
    }
}
```

</details>

<details>
<summary><b><code>migration {}</code></b></summary>

```kotlin
migration {
    // pick exactly one:

    flyway {
        locations     = listOf("filesystem:src/main/resources/db/migration")  // optional | default shown
        defaultSchema = "public"                                               // optional
        schemas       = listOf("public")                                       // optional
        table         = "flyway_schema_history"                                // optional
        properties    = mapOf("connectRetries" to "3")                        // optional | any flyway.* key, without prefix
    }

    // or

    liquibase {
        changeLogFile                  = "src/main/resources/db/changelog.xml"  // required
        defaultSchemaName              = "public"                                // optional
        liquibaseSchemaName            = "liquibase"                             // optional
        databaseChangeLogTableName     = "databasechangelog"                     // optional
        databaseChangeLogLockTableName = "databasechangeloglock"                 // optional
        parameters                     = mapOf("key" to "value")                // optional
    }

    // or

    sql {                                                                        // plain JDBC, no history or rollback
        scripts = fileTree("src/main/resources/db") { include("*.sql") }        // required | executed in order
    }
}
```

</details>

<details>
<summary><b><code>codegen {}</code></b></summary>

Mirrors the official `org.jooq.jooq-codegen-gradle` plugin's `configuration {}` block — copy your `generator { ... }` content here with minimal changes.

| Property | Default | Description |
|---|---|---|
| `logging` | `INFO` | `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL` |
| `onError` | `FAIL` | `FAIL`, `LOG`, `SILENT` |
| `configFile` | — | Base jOOQ XML config file; DSL properties override it. The `<jdbc>` block is always ignored — credentials are injected at runtime. |
| `generator.name` | `JavaGenerator` | Use `KotlinGenerator` or `ScalaGenerator` for other target languages |
| `generator.database.name` | auto-selected by database type | jOOQ meta class (e.g. `org.jooq.meta.postgres.PostgresDatabase`) |
| `generator.database.inputSchema` | — | Schema to introspect |
| `generator.database.includes` | `.*` | Regex of objects to include |
| `generator.database.excludes` | — | Regex of objects to exclude |
| `generator.generate.pojos` | `false` | Generate POJO classes |
| `generator.generate.immutablePojos` | `false` | Make POJOs immutable |
| `generator.generate.daos` | `false` | Generate DAO classes |
| `generator.generate.interfaces` | `false` | Generate interfaces for records/pojos |
| `generator.generate.records` | `true` | Generate Record classes |
| `generator.generate.javaTimeTypes` | `true` | Use `java.time` types instead of `java.sql` |
| `generator.generate.springAnnotations` | `false` | Add Spring `@Repository`/`@Autowired` annotations |
| `generator.generate.springDao` | `false` | Make DAOs Spring-injectable |
| `generator.generate.fluentSetters` | `false` | Generate fluent (chained) setters |
| `generator.generate.globalObjectReferences` | `true` | Generate global object reference classes |
| `generator.strategy.name` | — | Custom `GeneratorStrategy` class |
| `generator.target.packageName` | — (required) | Root package for generated classes |
| `generator.target.directory` | `build/generated-sources/jooq` | Output directory |

#### `forcedTypes {}`

```kotlin
forcedTypes {
    forcedType {
        name              = "DECIMAL_INTEGER"
        includeExpression = ".*\\.points_amount"
        includeTypes      = "BIGINT"
    }
}
```

| Property | Description |
|---|---|
| `name` | jOOQ built-in type (e.g. `BOOLEAN`, `JSON`) |
| `userType` | Fully-qualified Java class |
| `converter` | Fully-qualified `Converter` class |
| `binding` | Fully-qualified `Binding` class |
| `includeExpression` | Regex matching column names |
| `excludeExpression` | Regex to exclude column names |
| `includeTypes` | Regex matching SQL type names |
| `excludeTypes` | Regex to exclude SQL type names |
| `nullability` | `ALL`, `NOT_NULL`, `NULL` |
| `objectType` | `ALL`, `COLUMN`, `ATTRIBUTE`, `ELEMENT`, `PARAMETER`, `SEQUENCE` |

For Kotlin codegen, add `org.jooq:jooq-kotlin` to `implementation`.

</details>

---

## License

This plugin is licensed under the [Apache License 2.0](LICENSE).

**Plugin runtime dependencies** — used by the plugin itself to run migrations and generate code:
- **jOOQ OSS** (`jooqCodegen` default) — [Apache 2.0](https://github.com/jOOQ/jOOQ/blob/main/LICENSE)
- **Flyway Community** (`jooqMigration` default for Flyway) — [Apache 2.0](https://github.com/flyway/flyway/blob/main/LICENSE.txt)
- **Liquibase core** (`jooqMigration` default for Liquibase) — [FSL-1.1-ALv2](https://github.com/liquibase/liquibase/blob/main/LICENSE.txt)

**Default JDBC drivers** — resolved into your build only when no `jooqDatabase` dependency is declared:
- **PostgreSQL JDBC driver** (Postgres default) — [BSD 2-Clause](https://github.com/pgjdbc/pgjdbc/blob/master/LICENSE)
- **MySQL Connector/J** (MySQL default) — [GPL-2.0 with FOSS Exception](https://github.com/mysql/mysql-connector-j/blob/release/9.x/LICENSE)
- **MariaDB Connector/J** (MariaDB default) — [LGPL-2.1](https://github.com/mariadb-corporation/mariadb-connector-j/blob/main/LICENSE)
