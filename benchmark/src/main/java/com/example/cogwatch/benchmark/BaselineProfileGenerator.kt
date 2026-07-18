package com.example.cogwatch.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile for Cognization-Watch by exercising
 * the critical user journey during cold start.
 *
 * Usage:
 *   ./gradlew :benchmark:generateBaselineProfile \
 *     -P android.testInstrumentationRunnerArguments.class=\
 *   com.example.cogwatch.benchmark.BaselineProfileGenerator
 *
 * Output: app/src/release/generated/baselineProfiles/baseline-prof.txt
 * Copy to:  app/src/main/baseline-prof.txt
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateColdStartProfile() {
        baselineProfileRule.collect(
            packageName = TARGET_PACKAGE,
            maxIterations = 3,
        ) {
            pressHome()
            startActivityAndWait()
            // Wait for SplashActivity to finish routing to the next Activity
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), 15_000)

            // Allow the UI to settle so JIT captures the full startup path
            device.waitForIdle()
        }
    }

    companion object {
        private const val TARGET_PACKAGE = "com.example.cogwatch"
    }
}
