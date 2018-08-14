package com.awareframework.android.sensor.screen.model

import com.awareframework.android.core.model.AwareObject

/**
 * Contains the screen statuses data.
 *
 * @author  sercant
 * @date 14/08/2018
 */
data class ScreenData(
        /**
         * screen status, one of the following: 0=off, 1=on, 2=locked, 3=unlocked
         */
        var screenStatus: Int = 0
) : AwareObject(jsonVersion = 1) {

    companion object {
        const val TABLE_NAME = "screenData"
    }

    override fun toString(): String = toJson()
}