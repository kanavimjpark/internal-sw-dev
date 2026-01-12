package com.example.a3dmodelsample

import android.opengl.Matrix
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.view.SurfaceView
import com.google.android.filament.Camera
import com.google.android.filament.TransformManager
import com.google.android.filament.utils.ModelViewer
import kotlin.math.abs
import kotlin.math.sqrt

class AnchorProjector(
    private val modelViewer: ModelViewer,
    private val surfaceView: SurfaceView,
    private val rootLayout: FrameLayout,
    private val camera: Camera,
    private val cameraController: CameraController,
) {
    /** Blink-prevention state */
    private val shownState = mutableMapOf<String, Boolean>()

    // Tuning knobs (keep same behavior you had, but centralized)
    var centerTowardCameraBias: Float = 0.15f
    var topViewDotUp: Float = 0.7f
    var hideThreshold: Float = -0.12f
    var showThreshold: Float = -0.06f

    /**
     * Update one button anchor.
     * @param id stable id key (e.g., "door_front_left")
     * @param entity filament entity id
     * @param btn UI button
     * @param center shared "lookAt center" (must match cameraController.centerProvider())
     */
    fun updateEntityButton(
        id: String,
        entity: Int,
        btn: ImageButton,
        transformManager: TransformManager,
        center: FloatArray,
        yOffsetPx: Float = 0f
    ) {
        val instance = transformManager.getInstance(entity)
        if (instance == 0) {
            hide(btn, id)
            return
        }

        // --- world position (translation) from world transform matrix ---
        val worldTransform = FloatArray(16)
        transformManager.getWorldTransform(instance, worldTransform)
        val pos = floatArrayOf(worldTransform[12], worldTransform[13], worldTransform[14])

        // --- viewDir = normalize(eye - center) (center -> camera direction) ---
        var fx = cameraController.lastEye[0] - center[0]
        var fy = cameraController.lastEye[1] - center[1]
        var fz = cameraController.lastEye[2] - center[2]
        val len = sqrt(fx * fx + fy * fy + fz * fz).coerceAtLeast(1e-6f)
        fx /= len; fy /= len; fz /= len

        // Bias the center toward camera to make "behind" 판단 더 빠르게
        val biasedCenter = floatArrayOf(
            center[0] + fx * centerTowardCameraBias,
            center[1] + fy * centerTowardCameraBias,
            center[2] + fz * centerTowardCameraBias
        )

        // If top view, disable hide logic (show all)
        val isTopView = abs(fy) > topViewDotUp

        if (!isTopView) {
            val vx = pos[0] - biasedCenter[0]
            val vy = pos[1] - biasedCenter[1]
            val vz = pos[2] - biasedCenter[2]
            val dot = vx * fx + vy * fy + vz * fz

            val currentlyShown = shownState[id] ?: true
            val shouldHide = if (currentlyShown) (dot < hideThreshold) else (dot < showThreshold)

            if (shouldHide) {
                hide(btn, id)
                return
            } else {
                shownState[id] = true
            }
        } else {
            shownState[id] = true
        }

        // --- projection ---
        val screen = projectWorldToScreen(pos) ?: run {
            hide(btn, id)
            return
        }

        var x = screen.first
        var y = screen.second

        // SurfaceView coordinate -> rootLayout coordinate
        val svLoc = IntArray(2)
        val rootLoc = IntArray(2)
        surfaceView.getLocationInWindow(svLoc)
        rootLayout.getLocationInWindow(rootLoc)
        x += (svLoc[0] - rootLoc[0])
        y += (svLoc[1] - rootLoc[1])

        // Apply final position
        btn.x = x - btn.width / 2f
        btn.y = (y - yOffsetPx) - btn.height / 2f
        btn.visibility = View.VISIBLE
    }

    private fun hide(btn: ImageButton, id: String) {
        btn.visibility = View.GONE
        shownState[id] = false
    }

    /**
     * World -> screen (Android top-left origin).
     * Returns Pair(x, y) in SurfaceView pixel coords (top-left origin).
     */
    fun projectWorldToScreen(worldPos: FloatArray): Pair<Float, Float>? {
        // 0) Filament viewport
        val vp = modelViewer.view.viewport
        val vpX = vp.left
        val vpY = vp.bottom
        val vpW = vp.width
        val vpH = vp.height

        // 1) view/proj
        val viewD = DoubleArray(16).also { camera.getViewMatrix(it) }
        val projD = DoubleArray(16).also { camera.getProjectionMatrix(it) }
        val view = FloatArray(16) { viewD[it].toFloat() }
        val proj = FloatArray(16) { projD[it].toFloat() }

        // 2) VP = P * V
        val vpMat = FloatArray(16)
        Matrix.multiplyMM(vpMat, 0, proj, 0, view, 0)

        // 3) world -> clip
        val world4 = floatArrayOf(worldPos[0], worldPos[1], worldPos[2], 1f)
        val clip = FloatArray(4)
        Matrix.multiplyMV(clip, 0, vpMat, 0, world4, 0)
        val w = clip[3]
        if (w == 0f) return null

        // 4) NDC
        val ndcX = clip[0] / w
        val ndcY = clip[1] / w
        val ndcZ = clip[2] / w
        if (ndcZ < -1f || ndcZ > 1f) return null

        // 5) viewport coords (origin bottom-left)
        val vx = ((ndcX + 1f) * 0.5f) * vpW + vpX
        val vyGL = ((ndcY + 1f) * 0.5f) * vpH + vpY

        // 6) Android coords (origin top-left)
        val vyAndroid = (vpY + vpH) - vyGL

        return vx to vyAndroid
    }
}
