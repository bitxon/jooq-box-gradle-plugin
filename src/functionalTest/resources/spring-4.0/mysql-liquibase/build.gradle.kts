
plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
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
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    runtimeOnly("com.mysql:mysql-connector-j")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jooq {
    database {
        container {
            type = "MYSQL"
        }
    }
    migration {
        liquibase {
            changeLogFile = "src/main/resources/db/changelog/db.changelog-root.xml"
            defaultSchemaName = "test"
        }
    }
    codegen {
        generator {
            database {
                inputSchema = "test"
                excludes = "databasechangelog|databasechangeloglock"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
