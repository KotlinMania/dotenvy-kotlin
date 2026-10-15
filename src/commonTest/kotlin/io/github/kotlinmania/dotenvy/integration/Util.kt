// port-lint: tests integration/util/mod.rs
package io.github.kotlinmania.dotenvy.integration

public const val TEST_KEY: String = "TESTKEY"
public const val TEST_VALUE: String = "test_val"
public const val TEST_EXISTING_KEY: String = "TEST_EXISTING_KEY"
public const val TEST_EXISTING_VALUE: String = "from_env"
public const val TEST_OVERRIDING_VALUE: String = "from_file"

public fun createDefaultEnvfile(): String =
    "$TEST_KEY=$TEST_VALUE\n$TEST_EXISTING_KEY=$TEST_OVERRIDING_VALUE"

public fun createInvalidEnvfile(): String =
    "$TEST_KEY$TEST_VALUE\n$TEST_EXISTING_KEY$TEST_OVERRIDING_VALUE"
