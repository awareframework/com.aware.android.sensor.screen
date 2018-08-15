package com.awareframework.android.sensor.screen

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.awareframework.android.core.db.Engine
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 * <p>
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        ScreenSensor.startService(appContext, ScreenSensor.ScreenConfig().apply {
            sensorObserver = object : ScreenSensor.SensorObserver {
                override fun onScreenOn() {
                    // your code here...
                }

                override fun onScreenOff() {
                    // your code here...
                }

                override fun onScreenLocked() {
                    // your code here...
                }

                override fun onScreenUnlocked() {
                    // your code here...
                }
            }
            dbType = Engine.DatabaseType.ROOM
            debug = true
            // more configuration...
        })
    }
}
