package dev.bitxon.gradle.jooq.internal.worker

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.io.File
import java.sql.DriverManager

abstract class MigrationSqlAction : WorkAction<MigrationSqlAction.Params> {

    interface Params : WorkParameters {
        val jdbcUrl: Property<String>
        val jdbcUser: Property<String>
        val jdbcPassword: Property<String>
        val scriptPaths: ListProperty<String>
    }

    override fun execute() {
        val p = parameters
        DriverManager.getConnection(p.jdbcUrl.get(), p.jdbcUser.get(), p.jdbcPassword.get()).use { conn ->
            conn.autoCommit = false
            for (path in p.scriptPaths.get()) {
                val sql = File(path).readText()
                for (stmt in splitStatements(sql)) {
                    conn.createStatement().use { it.execute(stmt) }
                }
            }
            conn.commit()
        }
    }
}

internal fun splitStatements(sql: String): List<String> {
    val statements = mutableListOf<String>()
    val current = StringBuilder()
    var i = 0
    var inSingleQuote = false
    var inDoubleQuote = false
    var inBacktick = false
    var inLineComment = false
    var inBlockComment = false
    var dollarQuoteTag: String? = null

    while (i < sql.length) {
        val c = sql[i]
        when {
            inLineComment -> {
                if (c == '\n') inLineComment = false
                current.append(c)
            }
            inBlockComment -> {
                if (c == '*' && i + 1 < sql.length && sql[i + 1] == '/') {
                    current.append("*/")
                    i += 2
                    inBlockComment = false
                    continue
                }
                current.append(c)
            }
            inSingleQuote -> {
                current.append(c)
                if (c == '\\' && i + 1 < sql.length) {
                    // MySQL/MariaDB backslash escape — consume the next char without examining it
                    current.append(sql[i + 1])
                    i += 2
                    continue
                } else if (c == '\'' && i + 1 < sql.length && sql[i + 1] == '\'') {
                    current.append('\'')
                    i += 2
                    continue
                } else if (c == '\'') {
                    inSingleQuote = false
                }
            }
            inDoubleQuote -> {
                current.append(c)
                if (c == '"' && i + 1 < sql.length && sql[i + 1] == '"') {
                    current.append('"')
                    i += 2
                    continue
                } else if (c == '"') {
                    inDoubleQuote = false
                }
            }
            inBacktick -> {
                current.append(c)
                if (c == '`' && i + 1 < sql.length && sql[i + 1] == '`') {
                    current.append('`')
                    i += 2
                    continue
                } else if (c == '`') {
                    inBacktick = false
                }
            }
            dollarQuoteTag != null -> {
                val tag = dollarQuoteTag!!
                if (sql.startsWith(tag, i)) {
                    current.append(tag)
                    dollarQuoteTag = null
                    i += tag.length
                    continue
                }
                current.append(c)
            }
            c == '-' && i + 1 < sql.length && sql[i + 1] == '-' -> {
                inLineComment = true
                current.append("--")
                i += 2
                continue
            }
            c == '/' && i + 1 < sql.length && sql[i + 1] == '*' -> {
                inBlockComment = true
                current.append("/*")
                i += 2
                continue
            }
            c == '\'' -> {
                inSingleQuote = true
                current.append(c)
            }
            c == '"' -> {
                inDoubleQuote = true
                current.append(c)
            }
            c == '`' -> {
                inBacktick = true
                current.append(c)
            }
            c == '$' -> {
                val j = sql.indexOf('$', i + 1)
                if (j > i) {
                    val inner = sql.substring(i + 1, j)
                    val validTag = inner.isEmpty() ||
                        (inner[0].isLetter() || inner[0] == '_') &&
                        inner.all { it.isLetterOrDigit() || it == '_' }
                    if (validTag) {
                        val tag = sql.substring(i, j + 1)
                        current.append(tag)
                        dollarQuoteTag = tag
                        i = j + 1
                        continue
                    }
                }
                current.append(c)
            }
            c == ';' -> {
                val stmt = current.toString().trim()
                if (stmt.isNotEmpty()) statements.add(stmt)
                current.clear()
            }
            else -> current.append(c)
        }
        i++
    }
    val remaining = current.toString().trim()
    if (remaining.isNotEmpty()) statements.add(remaining)
    return statements
}
