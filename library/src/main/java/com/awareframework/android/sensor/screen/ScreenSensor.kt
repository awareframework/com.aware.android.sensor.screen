package com.awareframework.android.sensor.screen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.awareframework.android.core.AwareSensor
import com.awareframework.android.core.model.SensorConfig
import com.awareframework.android.sensor.screen.model.ScreenData


/**
 * The screen sensor monitors the screen statuses, such as turning on and off, locked and unlocked.
 *
 * @author  sercant
 * @date 14/08/2018
 */
class ScreenSensor : AwareSensor() {

    companion object {
        const val TAG = "AWARE::Screen"

        /**
         * Broadcasted event: screen is on
         */
        const val ACTION_AWARE_SCREEN_ON = "ACTION_AWARE_SCREEN_ON"

        /**
         * Broadcasted event: screen is off
         */
        const val ACTION_AWARE_SCREEN_OFF = "ACTION_AWARE_SCREEN_OFF"

        /**
         * Broadcasted event: screen is locked
         */
        const val ACTION_AWARE_SCREEN_LOCKED = "ACTION_AWARE_SCREEN_LOCKED"

        /**
         * Broadcasted event: screen is unlocked
         */
        const val ACTION_AWARE_SCREEN_UNLOCKED = "ACTION_AWARE_SCREEN_UNLOCKED"

        const val ACTION_AWARE_TOUCH_CLICKED = "ACTION_AWARE_TOUCH_CLICKED"
        const val ACTION_AWARE_TOUCH_LONG_CLICKED = "ACTION_AWARE_TOUCH_LONG_CLICKED"
        const val ACTION_AWARE_TOUCH_SCROLLED_UP = "ACTION_AWARE_TOUCH_SCROLLED_UP"
        const val ACTION_AWARE_TOUCH_SCROLLED_DOWN = "ACTION_AWARE_TOUCH_SCROLLED_DOWN"

        /**
         * Screen status: OFF = 0
         */
        const val STATUS_SCREEN_OFF = 0

        /**
         * Screen status: ON = 1
         */
        const val STATUS_SCREEN_ON = 1

        /**
         * Screen status: LOCKED = 2
         */
        const val STATUS_SCREEN_LOCKED = 2

        /**
         * Screen status: UNLOCKED = 3
         */
        const val STATUS_SCREEN_UNLOCKED = 3

        const val ACTION_AWARE_SCREEN_START = "com.awareframework.android.sensor.screen.SENSOR_START"
        const val ACTION_AWARE_SCREEN_STOP = "com.awareframework.android.sensor.screen.SENSOR_STOP"

        const val ACTION_AWARE_SCREEN_SET_LABEL = "com.awareframework.android.sensor.screen.SET_LABEL"
        const val EXTRA_LABEL = "label"

        const val ACTION_AWARE_SCREEN_SYNC = "com.awareframework.android.sensor.screen.SENSOR_SYNC"

        val CONFIG = Config()

        fun start(context: Context, config: Config? = null) {
            if (config != null)
                CONFIG.replaceWith(config)
            context.startService(Intent(context, ScreenSensor::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenSensor::class.java))
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            when (intent.action) {
                ACTION_AWARE_SCREEN_SET_LABEL -> {
                    intent.getStringExtra(EXTRA_LABEL)?.let {
                        CONFIG.label = it
                    }
                }

                ACTION_AWARE_SCREEN_SYNC -> onSync(intent)
            }
        }
    }

    private val screenMonitor = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            when (intent.action) {
                ACTION_SCREEN_ON -> {
                    val data = ScreenData().apply {
                        timestamp = System.currentTimeMillis()
                        label = CONFIG.label
                        deviceId = CONFIG.deviceId

                        screenStatus = STATUS_SCREEN_ON
                    }

                    dbEngine?.save(data, ScreenData.TABLE_NAME)

                    CONFIG.sensorObserver?.onScreenOn()
                    sendBroadcast(Intent(ACTION_AWARE_SCREEN_ON))
                }

                ACTION_SCREEN_OFF -> {
                    val data = ScreenData().apply {
                        timestamp = System.currentTimeMillis()
                        label = CONFIG.label
                        deviceId = CONFIG.deviceId

                        screenStatus = STATUS_SCREEN_OFF
                    }

                    dbEngine?.save(data, ScreenData.TABLE_NAME)

                    CONFIG.sensorObserver?.onScreenOff()
                    sendBroadcast(Intent(ACTION_AWARE_SCREEN_OFF))

                    //If the screen is off, we need to check if the phone is really locked, as some users don't use it at all.
                    val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    if (km.inKeyguardRestrictedInputMode()) {
                        val data = ScreenData().apply {
                            timestamp = System.currentTimeMillis()
                            label = CONFIG.label
                            deviceId = CONFIG.deviceId

                            screenStatus = STATUS_SCREEN_LOCKED
                        }

                        dbEngine?.save(data, ScreenData.TABLE_NAME)

                        CONFIG.sensorObserver?.onScreenLocked()
                        sendBroadcast(Intent(ACTION_AWARE_SCREEN_LOCKED))
                    }
                }

                ACTION_USER_PRESENT -> {
                    val data = ScreenData().apply {
                        timestamp = System.currentTimeMillis()
                        label = CONFIG.label
                        deviceId = CONFIG.deviceId

                        screenStatus = STATUS_SCREEN_UNLOCKED
                    }

                    dbEngine?.save(data, ScreenData.TABLE_NAME)

                    CONFIG.sensorObserver?.onScreenUnlocked()
                    sendBroadcast(Intent(ACTION_AWARE_SCREEN_UNLOCKED))
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        initializeDbEngine(CONFIG)

        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(ACTION_AWARE_SCREEN_SET_LABEL)
            addAction(ACTION_AWARE_SCREEN_SYNC)
        })

        registerReceiver(screenMonitor, IntentFilter().apply {
            addAction(ACTION_SCREEN_ON)
            addAction(ACTION_SCREEN_OFF)
            addAction(ACTION_USER_PRESENT)
        })

        logd("Screen service created!")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        //We can only get the touch events if accessibility service is enabled.
        if (CONFIG.touchStatus) {
            // TODO check if accessibility service is active
        }

        logd("Screen service is active.")

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        dbEngine?.close()

        unregisterReceiver(screenReceiver)
        unregisterReceiver(screenMonitor)

        logd("Screen service terminated.")
    }


    override fun onSync(intent: Intent?) {
        dbEngine?.startSync(ScreenData.TABLE_NAME)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    data class Config(
            var sensorObserver: Observer? = null,
            var touchStatus: Boolean = false
    ) : SensorConfig(dbPath = "aware_screen") {

        override fun <T : SensorConfig> replaceWith(config: T) {
            super.replaceWith(config)

            if (config is Config) {
                sensorObserver = config.sensorObserver
                touchStatus = config.touchStatus
            }
        }
    }

    interface Observer {
        fun onScreenOn()
        fun onScreenOff()
        fun onScreenLocked()
        fun onScreenUnlocked()
    }

    class ScreenSensorBroadcastReceiver : AwareSensor.SensorBroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return

            logd("Sensor broadcast received. action: " + intent?.action)

            when (intent?.action) {
                SENSOR_START_ENABLED -> {
                    logd("Sensor enabled: " + CONFIG.enabled)

                    if (CONFIG.enabled) {
                        start(context)
                    }
                }

                ACTION_AWARE_SCREEN_STOP,
                SENSOR_STOP_ALL -> {
                    logd("Stopping sensor.")
                    stop(context)
                }

                ACTION_AWARE_SCREEN_START -> {
                    start(context)
                }
            }
        }
    }

    override fun sendBroadcast(intent: Intent?) {
        intent?.let {
            logd(it.action)
        }

        super.sendBroadcast(intent)
    }
}

private fun logd(text: String) {
    if (ScreenSensor.CONFIG.debug) Log.d(ScreenSensor.TAG, text)
}

private fun logw(text: String) {
    Log.w(ScreenSensor.TAG, text)
}