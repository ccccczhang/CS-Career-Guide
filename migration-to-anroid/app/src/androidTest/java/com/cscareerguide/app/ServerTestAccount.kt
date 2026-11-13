package com.cscareerguide.app

import androidx.test.platform.app.InstrumentationRegistry

object ServerTestAccount {
    private val arguments = InstrumentationRegistry.getArguments()

    val username: String
        get() = requireNotNull(arguments.getString("testUsername")) {
            "Pass -Pandroid.testInstrumentationRunnerArguments.testUsername=<account>."
        }

    val password: String
        get() = requireNotNull(arguments.getString("testPassword")) {
            "Pass -Pandroid.testInstrumentationRunnerArguments.testPassword=<password>."
        }
}
