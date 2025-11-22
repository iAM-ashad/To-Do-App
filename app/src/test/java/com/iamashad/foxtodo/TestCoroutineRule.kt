package com.iamashad.foxtodo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule : TestWatcher() {
    val dispatcher = StandardTestDispatcher()
    val scope = TestScope(dispatcher)

    override fun starting(description: Description?) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }

    /**
     * Note: explicit return type and fully-qualified call avoid recursion/type-inference issues.
     */
    fun runTest(block: suspend TestScope.() -> Unit): Unit =
        kotlinx.coroutines.test.runTest(dispatcher) { block() }
}
