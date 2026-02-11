package com.example.a3dmodelsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import com.example.a3dmodelsample.retrofit.data.SeatIndex
import com.google.android.filament.Camera
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer

class RadioFragment : Fragment(R.layout.fragment_radio) {

    private lateinit var surfaceView: SurfaceView
    private lateinit var rootLayout: FrameLayout

    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    private var filamentInitialized = false

    private lateinit var camera: Camera
    private var cameraEntity: Int = 0

    private lateinit var cameraController: CameraController
    private lateinit var anchorProjector: AnchorProjector

    private var lastFrameNs = 0L

    // ---- Hold states ----
    private var slideDir = 0     // -1 front, +1 back, 0 stop
    private var reclineDir = 0   // -1 front, +1 back, 0 stop

    // ---- Pose states (0=front, 1=middle, 2=back) ----
    private var slidePos = 0.9f
    private var reclinePos = 0.9f

    private lateinit var btnLeft : ImageButton
    private lateinit var btnRight : ImageButton
    private lateinit var btnUp : ImageButton
    private lateinit var btnDown : ImageButton


    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            val dt = if (lastFrameNs == 0L) 0f else (frameTimeNanos - lastFrameNs) / 1_000_000_000f
            lastFrameNs = frameTimeNanos

            Log.d("SeatFragment","slidePos : $slidePos")
            Log.d("SeatFragment","reclinePos : $reclinePos")
            val animator = modelViewer.animator
            if (animator != null) {
                // 1) update positions by hold direction
                if (slideDir != 0) slidePos = updatePosByHold(slidePos, slideDir, dt, ::pickSlideDuration, 0.4f)
                if (reclineDir != 0) reclinePos = updatePosByHold(reclinePos, reclineDir, dt, ::pickReclineDuration,0.1f)

                // 2) apply both poses every frame (so both states are "locked")
                applySlidePose(animator, slidePos)
                applyReclinePose(animator, reclinePos)

                // 3) update once
                animator.updateBoneMatrices()
            }

            modelViewer.render(frameTimeNanos)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surfaceView = view.findViewById(R.id.surfaceView)
        rootLayout = view.findViewById(R.id.fl_surface_container)

        btnLeft = view.findViewById(R.id.btn_play_left)
        btnRight = view.findViewById(R.id.btn_play_right)
        btnUp = view.findViewById(R.id.btn_play_up)
        btnDown = view.findViewById(R.id.btn_play_down)

        surfaceView.setZOrderOnTop(false)
        Log.d("SeatFragment","slideDir: $slideDir")
        Log.d("SeatFragment","reclineDir: $reclineDir")

        // ---- Hold listeners (press = move, release = stop, pose stays) ----
        btnRight.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { slideDir = +1; true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { slideDir = 0; true }
                else -> false
            }
        }
        btnLeft.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { slideDir = -1; true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { slideDir = 0; true }
                else -> false
            }
        }

        btnUp.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { reclineDir = +1; true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { reclineDir = 0; true }
                else -> false
            }
        }
        btnDown.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { reclineDir = -1; true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { reclineDir = 0; true }
                else -> false
            }
        }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initModelViewer()
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                if (!::camera.isInitialized) return
                val aspect = width.toDouble() / height.toDouble()
                camera.setProjection(
                    60.0, aspect,
                    0.01, 400.0,
                    Camera.Fov.HORIZONTAL
                )
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    override fun onResume() {
        super.onResume()
        if (::choreographer.isInitialized) {
            lastFrameNs = 0L
            choreographer.postFrameCallback(frameCallback)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::choreographer.isInitialized) {
            choreographer.removeFrameCallback(frameCallback)
        }
        slideDir = 0
        reclineDir = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyFilament()
        filamentInitialized = false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initModelViewer() {
        if (filamentInitialized) {
            choreographer.postFrameCallback(frameCallback)
            return
        }
        filamentInitialized = true

        choreographer = Choreographer.getInstance()
        modelViewer = ModelViewer(surfaceView)

        cameraEntity = EntityManager.get().create()
        camera = modelViewer.engine.createCamera(cameraEntity)
        camera.setProjection(60.0, 16.0/9.0, 0.01, 400.0, Camera.Fov.HORIZONTAL)
        modelViewer.view.camera = camera

        cameraController = CameraController(camera).apply {
            centerProvider = {
                modelViewer.asset?.boundingBox?.center ?: floatArrayOf(0f, 0f, 0f)
            }
        }

        anchorProjector = AnchorProjector(
            modelViewer = modelViewer,
            surfaceView = surfaceView,
            rootLayout = rootLayout,
            camera = camera,
            cameraController = cameraController
        )

        Thread {
            try {
                val glbBuffer = readAsset("models/seat_adjustment_dark.glb")
                val iblBuffer = readAsset("envs/venetian_crossroads_2k/seat_output_folder_ibl.ktx")
                val skyBuffer = readAsset("envs/venetian_crossroads_2k/seat_output_folder_skybox.ktx")

                requireActivity().runOnUiThread {
                    loadGLBFromBuffer(glbBuffer)
                    loadEnvironmentFromBuffer(iblBuffer, skyBuffer)
                    choreographer.postFrameCallback(frameCallback)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun loadGLBFromBuffer(buffer: ByteBuffer) {
        modelViewer.loadModelGlb(buffer)

        val asset = modelViewer.asset ?: return
        val box = asset.boundingBox
        val c = box.center
        val half = box.halfExtent

        val centerAdjusted = floatArrayOf(
            c[0],
            c[1] + half[1] * 0.36f,
            c[2]
        )
        cameraController.centerProvider = { centerAdjusted }

        val sphereR = kotlin.math.sqrt(
            half[0]*half[0] + half[1]*half[1] + half[2]*half[2]
        )

        val radius = sphereR * 4.5f   // 2.8~3.8 튜닝

        cameraController.setState(
            yaw = 48f,
            pitch = 28f,
            radius = radius
        )

    }

    private fun loadEnvironmentFromBuffer(ibl: ByteBuffer, sky: ByteBuffer) {
        KTX1Loader.createIndirectLight(modelViewer.engine, ibl).apply {
            intensity = 50_000f
            modelViewer.scene.indirectLight = this
        }
        KTX1Loader.createSkybox(modelViewer.engine, sky).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = requireContext().assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private fun destroyFilament() {
        if (!::modelViewer.isInitialized) return
        try {
            val engine = modelViewer.engine
            if (::choreographer.isInitialized) {
                choreographer.removeFrameCallback(frameCallback)
            }

            modelViewer.asset?.let { asset ->
                val entityManager = EntityManager.get()
                asset.entities.forEach { entity ->
                    engine.destroyEntity(entity)
                    entityManager.destroy(entity)
                }
            }

            if (::camera.isInitialized) {
                engine.destroyCameraComponent(cameraEntity)
            }

            engine.destroy()
        } catch (e: Exception) {
            Log.w("SeatFragment", "destroyFilament error: ${e.message}")
        }
    }

    // ---------- Pose math helpers ----------

    private fun animDuration(index: Int): Float {
        val a = modelViewer.animator ?: return 1e-6f
        return a.getAnimationDuration(index).coerceAtLeast(1e-6f)
    }

    // 공통: pos를 dt에 따라 한 구간(1.0)을 duration초에 이동하도록 업데이트
    private fun updatePosByHold(
        pos: Float,
        dir: Int,
        dt: Float,
        durationPicker: (Float, Int) -> Float
    ): Float {
        val dur = durationPicker(pos, dir).coerceAtLeast(1e-6f)
        val deltaPos = (dt / dur) * dir
        return (pos + deltaPos).coerceIn(0f, 2f)
    }

    private fun updatePosByHold(
        pos: Float,
        dir: Int,
        dt: Float,
        durationPicker: (Float, Int) -> Float,
        speed: Float
    ): Float {
        val dur = durationPicker(pos, dir).coerceAtLeast(1e-6f)
        val deltaPos = (dt / dur) * dir * speed
        return (pos + deltaPos).coerceIn(0f, 2f)
    }


    // ---- Slide duration pick ----
    private fun pickSlideDuration(pos: Float, dir: Int): Float {
        return when {
            pos < 1f && dir > 0 -> animDuration(SeatIndex.SEAT_SLIDE_FRONT_TO_MIDDLE.type)   // (3)
            pos <= 1f && dir < 0 -> animDuration(SeatIndex.SEAT_SLIDE_MIDDLE_TO_FRONT.type) // (2)
            pos >= 1f && dir > 0 -> animDuration(SeatIndex.SEAT_SLIDE_MIDDLE_TO_BACK.type)  // (1)
            else -> animDuration(SeatIndex.SEAT_SLIDE_BACK_TO_MIDDLE.type)                  // (0)
        }
    }

    // ---- Recline duration pick ----
    private fun pickReclineDuration(pos: Float, dir: Int): Float {
        return when {
            pos < 1f && dir > 0 -> animDuration(SeatIndex.SEAT_RECLINE_FRONT_TO_MIDDLE.type)   // (5)
            pos <= 1f && dir < 0 -> animDuration(SeatIndex.SEAT_RECLINE_MIDDLE_TO_FRONT.type) // (4)
            pos >= 1f && dir > 0 -> animDuration(SeatIndex.SEAT_RECLINE_MIDDLE_TO_BACK.type)  // (6)
            else -> animDuration(SeatIndex.SEAT_RECLINE_BACK_TO_MIDDLE.type)                  // (7)
        }
    }

    private fun effectiveEndTime(index: Int): Float {
        val a = modelViewer.animator ?: return 0f
        val d = a.getAnimationDuration(index).coerceAtLeast(1e-6f)
        return if (index == SeatIndex.SEAT_SLIDE_MIDDLE_TO_BACK.type || index == SeatIndex.SEAT_RECLINE_MIDDLE_TO_BACK.type) d * 0.9f
        else d
    }

    private fun applySlidePose(animator: com.google.android.filament.gltfio.Animator, pos: Float) {
        if (pos <= 1f) {
            // 0..1 => use FRONT_TO_MIDDLE (3)
            val u = pos.coerceIn(0f, 1f)
            val dur = animDuration(SeatIndex.SEAT_SLIDE_FRONT_TO_MIDDLE.type)
            animator.applyAnimation(SeatIndex.SEAT_SLIDE_FRONT_TO_MIDDLE.type, u * dur)
        } else {
            // 1..2 => use MIDDLE_TO_BACK (1) with end-cut
            val u = (pos - 1f).coerceIn(0f, 1f)
            val idx = SeatIndex.SEAT_SLIDE_MIDDLE_TO_BACK.type
            val end = effectiveEndTime(idx)
            animator.applyAnimation(idx, (u * end).coerceIn(0f, end))
        }
    }

    private fun applyReclinePose(animator: com.google.android.filament.gltfio.Animator, pos: Float) {
        if (pos <= 1f) {
            // 0..1 => use FRONT_TO_MIDDLE (5)
            val u = pos.coerceIn(0f, 1f)
            val dur = animDuration(SeatIndex.SEAT_RECLINE_FRONT_TO_MIDDLE.type)
            Log.d("SeatFragment","")
            animator.applyAnimation(SeatIndex.SEAT_RECLINE_FRONT_TO_MIDDLE.type, u * dur)
        } else {
            // 1..2 => use MIDDLE_TO_BACK (6)
            val u = (pos - 1f).coerceIn(0f, 1f)
            val idx = SeatIndex.SEAT_RECLINE_MIDDLE_TO_BACK.type
            val end = effectiveEndTime(idx)
            animator.applyAnimation(idx, (u * end).coerceIn(0f, end))
        }
    }

}
