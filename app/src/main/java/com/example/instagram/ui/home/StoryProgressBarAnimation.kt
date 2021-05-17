package com.example.instagram.ui.home

import android.view.animation.ScaleAnimation
import android.view.animation.Transformation

/**
 * Created by Thanh Long Nguyen on 5/17/2021
 */
class StoryProgressBarAnimation
    (
    fromX: Float, toX: Float,
    fromY: Float, toY: Float,
    pivotXType: Int, pivotXValue: Float,
    pivotYType: Int, pivotYValue: Float,
) : ScaleAnimation(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType, pivotYValue) {

    private var elapsedTimeSincePause: Long = 0L
    private var isPaused: Boolean = false

    override fun getTransformation(
        currentTime: Long,
        outTransformation: Transformation?,
        scale: Float
    ): Boolean {
        if (isPaused && elapsedTimeSincePause == 0L) {
            elapsedTimeSincePause = currentTime - startTime
        }
        if (isPaused) {
            startTime = currentTime - elapsedTimeSincePause
        }
        return super.getTransformation(currentTime, outTransformation, scale)
    }


    fun pause() {
        if (isPaused) return
        elapsedTimeSincePause = 0L
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }
}