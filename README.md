# AWARE Screen

[![jitpack-badge](https://jitpack.io/v/awareframework/com.aware.android.sensor.screen.svg)](https://jitpack.io/#awareframework/com.aware.android.sensor.screen)

The screen sensor monitors the screen statuses, such as turning on and off, locked and unlocked.

## Public functions

### ScreenSensor

+ `start(context: Context, config: ScreenSensor.Config?)`: Starts the screen sensor with the optional configuration.
+ `stop(context: Context)`: Stops the service.

### ScreenSensor.Config

Class to hold the configuration of the sensor.

#### Fields

+ `sensorObserver: ScreenSensor.Observer`: Callback for live data updates.
+ `enabled: Boolean` Sensor is enabled or not. (default = false)
+ `debug: Boolean` enable/disable logging to `Logcat`. (default = false)
+ `label: String` Label for the data. (default = "")
+ `deviceId: String` Id of the device that will be associated with the events and the sensor. (default = "")
+ `dbEncryptionKey` Encryption key for the database. (default =String? = null)
+ `dbType: Engine` Which db engine to use for saving data. (default = `Engine.DatabaseType.NONE`)
+ `dbPath: String` Path of the database. (default = "aware_screen")
+ `dbHost: String` Host for syncing the database. (Defult = `null`)

## Broadcasts

+ `ScreenSensor.ACTION_AWARE_SCREEN_ON` fired when the screen is on.
+ `ScreenSensor.ACTION_AWARE_SCREEN_OFF` fired when the screen is off.
+ `ScreenSensor.ACTION_AWARE_SCREEN_LOCKED` fired when the screen is locked.
+ `ScreenSensor.ACTION_AWARE_SCREEN_UNLOCKED` fired when the screen is unlocked.

## Data Representations

### Screen Data

Contains the screen profiles.

| Field        | Type   | Description                                                            |
| ------------ | ------ | ---------------------------------------------------------------------- |
| screenStatus | Int    | screen status, one of the following: 0=off, 1=on, 2=locked, 3=unlocked |
| deviceId     | String | AWARE device UUID                                                      |
| label        | String | Customizable label. Useful for data calibration or traceability        |
| timestamp    | Long   | unixtime milliseconds since 1970                                       |
| timezone     | Int    | [Raw timezone offset][1] of the device                                 |
| os           | String | Operating system of the device (ex. android)                           |

## Example usage

```kotlin
// To start the service.
ScreenSensor.start(appContext, ScreenSensor.Config().apply {
    sensorObserver = object : ScreenSensor.Observer {
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

// To stop the service
ScreenSensor.stop(appContext)
```

## License

Copyright (c) 2018 AWARE Mobile Context Instrumentation Middleware/Framework (http://www.awareframework.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[1]: https://developer.android.com/reference/java/util/TimeZone#getRawOffset()
