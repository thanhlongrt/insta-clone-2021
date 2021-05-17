package com.example.instagram.ui.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.example.instagram.R

/**
 * Created by Thanh Long Nguyen on 5/17/2021
 */
class StoryProgressBar(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val DEFAULT_PROGRESS_DURATION = 2000L
    }

    private  var frontProgressView: View
    private  var maxProgressView: View

    private var animation: StoryProgressBarAnimation? = null
    var duration: Long = DEFAULT_PROGRESS_DURATION
    var callback: Callback? = null

    interface Callback {
        fun onStartProgress()
        fun onFinishProgress()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.story_progress_bar, this)
        frontProgressView = findViewById(R.id.progress)
        maxProgressView = findViewById(R.id.max_progress)
    }

    fun setMax() {
        finishProgress(true)
    }

    fun setMin() {
        finishProgress(false)
    }

    fun setMinWithoutCallback() {
        maxProgressView.setBackgroundResource(R.color.dark_gray)
        maxProgressView.visibility = VISIBLE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
        }
    }

    fun setMaxWithoutCallback() {
        maxProgressView.setBackgroundResource(R.color.white)
        maxProgressView.visibility = VISIBLE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
        }
    }

    private fun finishProgress(isMax: Boolean) {
        if (isMax) maxProgressView.setBackgroundResource(R.color.white)
        maxProgressView.visibility = if (isMax) VISIBLE else GONE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
            if (callback != null) {
                callback!!.onFinishProgress()
            }
        }
    }

    fun startProgress() {
        maxProgressView.visibility = GONE
        animation = StoryProgressBarAnimation(
            0F, 1F,
            1F, 1F,
            Animation.ABSOLUTE, 0F,
            Animation.RELATIVE_TO_SELF, 0F
        )
        animation?.apply {
            duration = this@StoryProgressBar.duration
            interpolator = LinearInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    frontProgressView.visibility = VISIBLE
                    callback?.onStartProgress()
                }

                override fun onAnimationEnd(animation: Animation?) {
                    callback?.onFinishProgress()
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
            fillAfter = true
        }
        frontProgressView.startAnimation(animation)
    }

    fun pause(){
        animation?.pause()
    }

    fun resume(){
        animation?.resume()
    }

    fun clear() {
        animation?.let {
            it.setAnimationListener(null)
            it.cancel()
            animation = null
        }
    }
}