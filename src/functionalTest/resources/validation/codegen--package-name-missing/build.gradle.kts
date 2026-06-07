// Intentionally omits codegen.generator.target.packageName and codegen.configFile.
// The task should fail at execution time — packageName is required when no configFile is provided.
plugins {
    java
    id("dev.bitxon.jooq-box")
}

repositories {
    mavenCentral()
}

jooq {
    database {
        embedded { type = "H2" }
    }
    migration {
        flyway { }
    }
    // codegen.generator.target.packageName is intentionally missing, and no configFile is set
}
