package dev.bitxon.gradle.jooq.internal.database

internal interface Database {
    val jdbcUrl: String
    val username: String
    val password: String
    val jdbcDriver: String
    val description: String
    fun start()
    fun stop()
}
