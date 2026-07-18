package com.example.cogwatch.benchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold-start benchmark for Cognization-Watch.
 *
 * Run 10 iterations in COLD start mode, measuring timeToInitialDisplay (TTID).
 * Results include P50, P90, P99 in the benchmark JSON output.
 *
 * Usage:
 *   ./gradlew :benchmark:connectedCheck \
 *     -P android.testInstrumentationRunnerArguments.class=\
 *   com.example.cogwatch.benchmark.StartupBenchmark#startupCold
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Measures cold start without any Baseline Profile.
     * This represents the "before optimization" baseline.
     */
    @Test
    fun startupCold() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.None(),
            iterations = 10,
            startupMode = StartupMode.COLD,
            setupBlock = {
                pressHome()
            }
        ) {
            pressHome()
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(TARGET_PACKAGE)
            }
            startActivityAndWait(intent)

            // Wait until the app UI is visible (SplashActivity → LoginActivity or MainActivity)
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), 15_000)
        }
    }

    /**
     * Measures cold start with Baseline Profile applied.
     * Run after generating baseline-prof.txt and rebuilding.
     */
    @Test
    fun startupColdWithProfile() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = androidx.benchmark.macro.BaselineProfileMode.Require,
                warmupIterations = 3
            ),
            iterations = 10,
            startupMode = StartupMode.COLD,
            setupBlock = {
                pressHome()
            }
        ) {
            pressHome()
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(TARGET_PACKAGE)
            }
            startActivityAndWait(intent)
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), 15_000)
        }
    }

    companion object {
        private const val TARGET_PACKAGE = "com.example.cogwatch"
    }
}
