package dev.bitxon.gradle.jooq.internal.worker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MigrationSqlSplitterTest {

    // ── basic ─────────────────────────────────────────────────────────────────

    @Test
    fun `splits two statements`() {
        val result = splitStatements("SELECT 1; SELECT 2")
        assertThat(result).containsExactly("SELECT 1", "SELECT 2")
    }

    @Test
    fun `ignores trailing semicolon`() {
        val result = splitStatements("SELECT 1;")
        assertThat(result).containsExactly("SELECT 1")
    }

    @Test
    fun `returns empty list for blank input`() {
        assertThat(splitStatements("")).isEmpty()
        assertThat(splitStatements("   ")).isEmpty()
    }

    @Test
    fun `statement without semicolon is returned as-is`() {
        val result = splitStatements("SELECT 1")
        assertThat(result).containsExactly("SELECT 1")
    }

    // ── single-quoted strings ─────────────────────────────────────────────────

    @Test
    fun `semicolon inside single-quoted string is not a split point`() {
        val result = splitStatements("INSERT INTO t VALUES ('a;b'); SELECT 1")
        assertThat(result).containsExactly("INSERT INTO t VALUES ('a;b')", "SELECT 1")
    }

    @Test
    fun `escaped single quote inside string is handled`() {
        val result = splitStatements("INSERT INTO t VALUES ('it''s;here'); SELECT 1")
        assertThat(result).containsExactly("INSERT INTO t VALUES ('it''s;here')", "SELECT 1")
    }

    // ── double-quoted identifiers ─────────────────────────────────────────────

    @Test
    fun `semicolon inside double-quoted identifier is not a split point`() {
        val result = splitStatements("""CREATE TABLE t ("col;name" TEXT); SELECT 1""")
        assertThat(result).containsExactly("""CREATE TABLE t ("col;name" TEXT)""", "SELECT 1")
    }

    @Test
    fun `escaped double quote inside identifier is handled`() {
        val result = splitStatements("""CREATE TABLE t ("col""name;x" TEXT); SELECT 1""")
        assertThat(result).containsExactly("""CREATE TABLE t ("col""name;x" TEXT)""", "SELECT 1")
    }

    // ── backtick-quoted identifiers ───────────────────────────────────────────

    @Test
    fun `semicolon inside backtick-quoted identifier is not a split point`() {
        val result = splitStatements("CREATE TABLE t (`col;name` TEXT); SELECT 1")
        assertThat(result).containsExactly("CREATE TABLE t (`col;name` TEXT)", "SELECT 1")
    }

    @Test
    fun `escaped backtick inside identifier is handled`() {
        val result = splitStatements("CREATE TABLE t (`col``name;x` TEXT); SELECT 1")
        assertThat(result).containsExactly("CREATE TABLE t (`col``name;x` TEXT)", "SELECT 1")
    }

    // ── dollar-quoted strings (PostgreSQL) ───────────────────────────────────

    @Test
    fun `semicolon inside dollar-quoted block is not a split point`() {
        val result = splitStatements("SELECT \$\$a;b\$\$; SELECT 1")
        assertThat(result).containsExactly("SELECT \$\$a;b\$\$", "SELECT 1")
    }

    @Test
    fun `named dollar-quote tag is supported`() {
        val result = splitStatements("SELECT \$tag\$a;b\$tag\$; SELECT 1")
        assertThat(result).containsExactly("SELECT \$tag\$a;b\$tag\$", "SELECT 1")
    }

    @Test
    fun `plpgsql function body with semicolons inside dollar-quote`() {
        val sql = "CREATE FUNCTION f() RETURNS void AS \$\$BEGIN; END;\$\$ LANGUAGE plpgsql; SELECT 1"
        val result = splitStatements(sql)
        assertThat(result).containsExactly(
            "CREATE FUNCTION f() RETURNS void AS \$\$BEGIN; END;\$\$ LANGUAGE plpgsql",
            "SELECT 1"
        )
    }

    @Test
    fun `dollar parameter placeholder is not a dollar-quote`() {
        val result = splitStatements("SELECT \$1; SELECT \$2")
        assertThat(result).containsExactly("SELECT \$1", "SELECT \$2")
    }

    // ── backslash-escaped quotes (MySQL/MariaDB dialect) ─────────────────────

    @Test
    fun `backslash-escaped quote does not close single-quoted string`() {
        val result = splitStatements("""INSERT INTO t VALUES ('it\'s fine'); SELECT 1""")
        assertThat(result).containsExactly("""INSERT INTO t VALUES ('it\'s fine')""", "SELECT 1")
    }

    @Test
    fun `semicolon after backslash-escaped quote is not a split point`() {
        val result = splitStatements("""INSERT INTO t VALUES ('it\'s; fine'); SELECT 1""")
        assertThat(result).containsExactly("""INSERT INTO t VALUES ('it\'s; fine')""", "SELECT 1")
    }

    @Test
    fun `escaped backslash before closing quote closes the string`() {
        val result = splitStatements("""INSERT INTO t VALUES ('\\'); SELECT 1""")
        assertThat(result).containsExactly("""INSERT INTO t VALUES ('\\')""", "SELECT 1")
    }

    // ── comments ─────────────────────────────────────────────────────────────

    @Test
    fun `semicolon inside line comment is not a split point`() {
        val result = splitStatements("SELECT 1 -- this; is a comment\n; SELECT 2")
        assertThat(result).containsExactly("SELECT 1 -- this; is a comment", "SELECT 2")
    }

    @Test
    fun `semicolon inside block comment is not a split point`() {
        val result = splitStatements("SELECT 1 /* this; is a comment */; SELECT 2")
        assertThat(result).containsExactly("SELECT 1 /* this; is a comment */", "SELECT 2")
    }
}
