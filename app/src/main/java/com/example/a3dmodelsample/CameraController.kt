package com.example.a3dmodelsample

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.google.android.filament.Camera
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CameraController(
    private val camera: Camera,
) {
    // Camera state
    var yawDeg: Float = 0f
        private set
    var pitchDeg: Float = 30f
        private set
    var radius: Float = 9f
        private set

    // Cached eye (world)
    val lastEye: FloatArray = FloatArray(3)

    // Target center provider (you can inject boundingBox center + offsets)
    var centerProvider: () -> FloatArray = { floatArrayOf(0f, 0f, 0f) }

    private var animator: ValueAnimator? = null

    fun setState(yaw: Float, pitch: Float, radius: Float) {
        this.yawDeg = yaw
        this.pitchDeg = pitch
        this.radius = radius
        apply()
    }

    fun apply() {
        val center = centerProvider()

        val radYaw = Math.toRadians(yawDeg.toDouble())
        val radPitch = Math.toRadians(pitchDeg.toDouble())

        val eyeX = (radius * cos(radPitch) * cos(radYaw)).toFloat()
        val eyeY = (radius * sin(radPitch)).toFloat()
        val eyeZ = (radius * cos(radPitch) * sin(radYaw)).toFloat()

        camera.lookAt(
            eyeX.toDouble(), eyeY.toDouble(), eyeZ.toDouble(),
            center[0].toDouble(), center[1].toDouble(), center[2].toDouble(),
            0.0, 1.0, 0.0
        )

        lastEye[0] = eyeX
        lastEye[1] = eyeY
        lastEye[2] = eyeZ
    }

    fun cancelAnimation() {
        animator?.cancel()
        animator = null
    }

    /** Normalize angle to [0, 360) */
    fun normalizeAngle(deg: Float): Float {
        var a = deg % 360f
        if (a < 0f) a += 360f
        return a
    }

    /** Returns a "continuous" target angle that moves shortest path from 'from' to 'to'. */
    fun makeShortestTarget(from: Float, to: Float): Float {
        val f = normalizeAngle(from)
        val t = normalizeAngle(to)
        val delta = (t - f + 540f) % 360f - 180f  // [-180, 180]
        return from + delta
    }

    /**
     * Animate yaw + radius together.
     * - pitch stays as current pitch (or you can pass pitch too if you want).
     */
    fun animateRotateAndZoom(
        toYaw: Float,
        toRadius: Float,
        durationMs: Long = 500,
        onUpdate: (() -> Unit)? = null,
        onEnd: (() -> Unit)? = null
    ) {
        val fromYaw = yawDeg
        val fromRadius = radius
        val targetYawContinuous = makeShortestTarget(fromYaw, toYaw)

        cancelAnimation()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                val t = va.animatedValue as Float
                yawDeg = fromYaw + (targetYawContinuous - fromYaw) * t
                radius = fromRadius + (toRadius - fromRadius) * t
                apply()
                onUpdate?.invoke()
            }
            doOnEndCompat {
                yawDeg = normalizeAngle(toYaw)
                radius = toRadius
                apply()
                onUpdate?.invoke()
                onEnd?.invoke()
            }
        }
        animator?.start()
    }

    fun animateRotateOnly(
        toYaw: Float,
        durationMs: Long = 500,
        onUpdate: (() -> Unit)? = null,
        onEnd: (() -> Unit)? = null
    ) {
        val fromYaw = yawDeg
        val targetYawContinuous = makeShortestTarget(fromYaw, toYaw)

        cancelAnimation()
        animator = ValueAnimator.ofFloat(fromYaw, targetYawContinuous).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                yawDeg = va.animatedValue as Float
                apply()
                onUpdate?.invoke()
            }
            doOnEndCompat {
                yawDeg = normalizeAngle(toYaw)
                apply()
                onUpdate?.invoke()
                onEnd?.invoke()
            }
        }
        animator?.start()
    }

    fun rotateBy(
        deltaYaw: Float,
        deltaPitch: Float,
        pitchMin: Float = 20f,
        pitchMax: Float = 45f
    ) {
        yawDeg += deltaYaw
        pitchDeg = (pitchDeg + deltaPitch).coerceIn(pitchMin, pitchMax)
        apply()
    }

    fun zoomBy(
        deltaRadius: Float,
        minRadius: Float = 5f,
        maxRadius: Float = 10f
    ) {
        radius = (radius + deltaRadius).coerceIn(minRadius, maxRadius)
        apply()
    }




    // Tiny compat helper (avoids importing androidx.core in this file if you don't want it)
    private fun ValueAnimator.doOnEndCompat(block: () -> Unit) {
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) = block()
        })
    }
}
