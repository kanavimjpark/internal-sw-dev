package com.example.a3dmodelsample

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.content.res.Resources
import android.util.Log
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a3dmodelsample.retrofit.MainViewModelFactory
import com.example.a3dmodelsample.retrofit.NewsRepository
import com.example.a3dmodelsample.retrofit.RetrofitClient
import com.example.a3dmodelsample.retrofit.TickerWsClient
import com.example.a3dmodelsample.retrofit.WeatherRepository
import com.example.a3dmodelsample.viewmodel.MainViewModel
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer
import com.google.android.filament.Camera
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.Mat4
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    // UI components
    private lateinit var surfaceView: SurfaceView
    //for testing stock info
    private lateinit var tvTitle: TextView

    private lateinit var btnMedia: LinearLayout
    private lateinit var btnSetting: ConstraintLayout
    private lateinit var rootLayout: FrameLayout
    private lateinit var btnDoorFrontLeft: ImageButton
    private lateinit var btnDoorFrontLeftTransp: ImageButton
    // Filament ModelViewer & Choreographer
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    private lateinit var tickerWsClient: TickerWsClient

    // Animation control variables
    private var carLiftAnimationIndex = 0
    private var carLiftAnimationSpeed = 1
    private var carLiftAnimationStartTime = 0L
    private var loopAnimation = false
    private var isAnimationPlaying = false

    private lateinit var camera: Camera
    private var cameraEntity: Int = 0

    private var currentAngle = 0f
    private var radius = 6.0f  // 기본 줌 거리
    private var angleDegree = 0
    private var currentPitch = 40f

    private val NEWS_API_KEY = "dd07ab437c704c74babae5f73df37976"
    private val WEATHER_API_KEY = "16b7d1ccd4c3e4f4f42e2051cb5fe5dd"

    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    // Frame callback for animation update
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(currentTime: Long) {
//            if (!isRendering) return
            choreographer.postFrameCallback(this)

            if (isAnimationPlaying) {
                val elapsedSeconds = (currentTime - carLiftAnimationStartTime).toDouble() / 1_000_000_000

                modelViewer.animator?.apply {
                    if (animationCount > 0) {
                        val duration = getAnimationDuration(carLiftAnimationIndex)
                        if (duration != 0f) {
                            val timeInAnim = if (loopAnimation) {
                                (elapsedSeconds * carLiftAnimationSpeed % duration)
                            } else {
                                (elapsedSeconds * carLiftAnimationSpeed).coerceAtMost(duration.toDouble())
                            }

                            applyAnimation(carLiftAnimationIndex, timeInAnim.toFloat())
                        }
                    }
                    updateBoneMatrices()
                    updateButtonPositions()
                }
            }

            modelViewer.render(currentTime)
        }

        private fun Int.getTransform(): Mat4 {
            val tm = modelViewer.engine.transformManager
            return Mat4.of(*tm.getTransform(tm.getInstance(this), null as FloatArray?))
        }

        private fun Int.setTransform(mat: Mat4) {
            val tm = modelViewer.engine.transformManager
            tm.setTransform(tm.getInstance(this), mat.toFloatArray())
        }
    }

    // Entry point
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupSystemInsets()
        setupViewModel()
        initViews()
        val metrics = Resources.getSystem().displayMetrics
        val widthPixels = metrics.widthPixels
        val heightPixels = metrics.heightPixels

        Log.d("ScreenInfo", "Screen size: ${widthPixels} x ${heightPixels}")

        val configuration = resources.configuration
        val smallestScreenWidthDp = configuration.smallestScreenWidthDp
        Log.d("Screen", "sw: $smallestScreenWidthDp dp")



        val density = metrics.density
        val widthDp = (widthPixels / density).toInt()

        Log.d("Screen", "w: ${widthDp} dp")
    }

    override fun onStart() {
        super.onStart()
        tickerWsClient.connect()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        stopAnimationLoop()
    }

    override fun onStop() {
        super.onStop()
        destroyFilament()
        tickerWsClient.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimationLoop()
    }

    private fun setupViewModel() {
        // Create API clients
        val newsApi = RetrofitClient.createNewsApi(NEWS_API_KEY)

        // Create repositories
        val weatherRepo = WeatherRepository(
            apiKey = WEATHER_API_KEY
        )
        val newsRepo = NewsRepository(
            api = newsApi,
            apiKey = NEWS_API_KEY
        )

        // Create ViewModel via factory
        val factory = MainViewModelFactory(weatherRepo, newsRepo)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    // Setup system insets (padding for status bar, etc.)
    private fun setupSystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Initialize all UI and model-related views
    private fun initViews() {
        tickerWsClient = TickerWsClient("d48prghr01qnpsnov7j0d48prghr01qnpsnov7jg") { symbol, price, ts ->
            runOnUiThread {
                tvTitle.text = "$symbol : $price"
            }
        }

        mainViewModel.loadWeather("Seoul,KR")

        mainViewModel.loadNews("us")

        mainViewModel.weatherLiveData.observe(this, Observer {weather ->
            Log.d("MainActivity", "🌤 Weather Temp = ${weather.main?.temp}")
        })
        mainViewModel.newsLiveData.observe(this, Observer { news ->
            Log.d("MainActivity", "📰 Top Headline = ${news.articles?.firstOrNull()?.title}")
        })


        // Bind views
        surfaceView = findViewById(R.id.surfaceView)
        tvTitle = findViewById(R.id.tv_title)
        btnMedia = findViewById(R.id.bt_list)
        btnSetting = findViewById(R.id.manual_widget)
        rootLayout = findViewById(R.id.fl_surface_container)
        // Assign button listeners
//        btnDown.setOnClickListener { playAnimation(ANIM_DOWN) }
//        btnAni.setOnClickListener { isAnimationPlaying = false}
//        btnIdleFinal.setOnClickListener { zoomIn() }
//        btnIdleMid.setOnClickListener { zoomOut() }
//        btnIdUp.setOnClickListener { turnLeftCamera() }
//
//        btnSpeedUp.setOnClickListener { carLiftAnimationSpeed *= 2 }
//        btnSpeedDown.setOnClickListener { turnRightCamera() }

        btnMedia.setOnClickListener {
            stopAnimationLoop()
            val intent = Intent(this@MainActivity, MusicActivity::class.java)
            startActivity(intent) }

        btnSetting.setOnClickListener {
            stopAnimationLoop()
            val intent = Intent(this@MainActivity, SettingActivity::class.java)
            startActivity(intent) }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initModelViewer() // 이 안에서 Filament 엔진 및 GLB 로딩 시작
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    // Initialize ModelViewer and environment
    @SuppressLint("ClickableViewAccessibility")
    private fun initModelViewer() {
        choreographer = Choreographer.getInstance()

        modelViewer = ModelViewer(surfaceView)

        gestureDetector = GestureDetector(this, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        cameraEntity = EntityManager.get().create()
        camera = modelViewer.engine.createCamera(cameraEntity)
        camera.setProjection(100.0, 1280.0 / 720.0, 0.9, 100.0, Camera.Fov.HORIZONTAL) // 종횡비는 화면 비율에 맞게 조정
        modelViewer.view.camera = camera
        surfaceView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            scaleGestureDetector.onTouchEvent(event)
            true
        }
//        turnLeftCameraAngle()
        Thread {
            try {
                val glbBuffer = readAsset("models/animation_separate.glb")
                val iblBuffer = readAsset("envs/venetian_crossroads_2k/output_folder_ibl.ktx")
                val skyBuffer = readAsset("envs/venetian_crossroads_2k/output_folder_skybox.ktx")

                runOnUiThread {
                    loadGLBFromBuffer(glbBuffer)
                    loadEnvironmentFromBuffer(iblBuffer, skyBuffer)
                    choreographer.postFrameCallback(frameCallback)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to load 3D resources", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun loadGLBFromBuffer(buffer: ByteBuffer) {
        val scale =1f
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()

        val root = modelViewer.asset?.root
        root?.let {
            val transformManager = modelViewer.engine.transformManager
            val instance = transformManager.getInstance(it)

            val scaleMatrix = floatArrayOf(
                scale, 0f, 0f, 0f,
                0f, scale, 0f, 0f,
                0f, 0f, scale, 0f,
                0f, 0f, 0f, 1f
            )

            transformManager.setTransform(instance, scaleMatrix)
        }

        turnRightCameraAngle()

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

    // Play selected animation

    private fun playAnimation(index: Int, isLoop: Boolean = true) {
        val animator = modelViewer.animator
        if (animator == null) {
            Toast.makeText(this, "Animator not initialized", Toast.LENGTH_LONG).show()
            return
        }

        val animationCount = animator.animationCount
        if (index < 0 || index >= animationCount) {
            Toast.makeText(this, "Invalid animation index: $index", Toast.LENGTH_LONG).show()
            return
        }

        isAnimationPlaying = true
        carLiftAnimationIndex = index
        loopAnimation = isLoop
        carLiftAnimationStartTime = System.nanoTime()

        val duration = animator.getAnimationDuration(index)
        val name = animator.getAnimationName(index)

        if (duration == 0f) {
            Toast.makeText(this, "Animation '$name' has zero duration.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Playing '$name' ($duration seconds)", Toast.LENGTH_LONG).show()
        }
    }

    fun applyAllAnimations() {
        val animator = modelViewer.animator
        if (animator == null) {
            Toast.makeText(this, "Animator not initialized", Toast.LENGTH_LONG).show()
            return
        }
        val animationCount = animator.animationCount
        val time = System.nanoTime() / 1_000_000_000f
        for (i in 0 until animationCount) {
            animator.applyAnimation(i, time)
        }
        animator.updateBoneMatrices()
    }

    // 월드 좌표 3개짜리 FloatArray → 화면 좌표 (Pair<Float, Float>), 화면 밖이면 null
    private fun projectWorldToScreen(worldPos: FloatArray): Pair<Float, Float>? {
        if (!::camera.isInitialized) return null

        val viewMatrix = DoubleArray(16)
        camera.getViewMatrix(viewMatrix)

        val projMatrix = DoubleArray(16)
        camera.getProjectionMatrix(projMatrix)

        val vpMatrix = DoubleArray(16)
        // Double 배열끼리 곱셈을 위해 별도 함수 필요
        multiplyMMDouble(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        val worldVec = doubleArrayOf(worldPos[0].toDouble(), worldPos[1].toDouble(), worldPos[2].toDouble(), 1.0)
        val clipCoords = DoubleArray(4)
        multiplyMVDouble(clipCoords, 0, vpMatrix, 0, worldVec, 0)

        if (clipCoords[3] == 0.0) return null
        val ndcX = clipCoords[0] / clipCoords[3]
        val ndcY = clipCoords[1] / clipCoords[3]
        val ndcZ = clipCoords[2] / clipCoords[3]

        if (ndcX < -1.0 || ndcX > 1.0 || ndcY < -1.0 || ndcY > 1.0 || ndcZ < 0.0 || ndcZ > 1.0) return null

        val width = surfaceView.width.toFloat()
        val height = surfaceView.height.toFloat()

        val screenX = ((ndcX + 1.0) / 2.0 * width).toFloat()
        val screenY = ((1.0 - ndcY) / 2.0 * height).toFloat()
//        Log.d("mjpark", "screenX: $screenX, screenY: $screenY")
        return Pair(screenX, screenY)
    }

    // 4x4 행렬 곱셈 (Double)
    private fun multiplyMMDouble(
        result: DoubleArray, resultOffset: Int,
        lhs: DoubleArray, lhsOffset: Int,
        rhs: DoubleArray, rhsOffset: Int
    ) {
        for (i in 0..3) {
            for (j in 0..3) {
                var sum = 0.0
                for (k in 0..3) {
                    sum += lhs[lhsOffset + i * 4 + k] * rhs[rhsOffset + k * 4 + j]
                }
                result[resultOffset + i * 4 + j] = sum
            }
        }
    }

    // 4x4 행렬 * 4x1 벡터 곱셈 (Double)
    private fun multiplyMVDouble(
        resultVec: DoubleArray, resultVecOffset: Int,
        mat: DoubleArray, matOffset: Int,
        vec: DoubleArray, vecOffset: Int
    ) {
        for (i in 0..3) {
            var sum = 0.0
            for (j in 0..3) {
                sum += mat[matOffset + i * 4 + j] * vec[vecOffset + j]
            }
            resultVec[resultVecOffset + i] = sum
        }
    }


    private fun updateButtonPositions() {
        val asset = modelViewer.asset ?: return
        val transformManager = modelViewer.engine.transformManager

        // door_front_left_obj001 월드 좌표 찾기

        val doorFrontLeftEntity = if (asset.getName(61) == "door_front_left_obj001") {
            asset.entities[61]
        } else {
            null
        }


        // door_front_left_obj001_transp 월드 좌표 찾기

        val doorFrontLeftTranspEntity = if (asset.entities.size > 62) asset.entities[62] else null


        doorFrontLeftEntity?.let {
            val instance = transformManager.getInstance(it)
            val worldTransform = FloatArray(16)
            transformManager.getWorldTransform(instance, worldTransform)
            val pos = floatArrayOf(worldTransform[12], worldTransform[13], worldTransform[14])

            projectWorldToScreen(pos)?.let { (x, y) ->
                btnDoorFrontLeft.x = x - btnDoorFrontLeft.width / 2f
                btnDoorFrontLeft.y = y - btnDoorFrontLeft.height / 2f
                btnDoorFrontLeft.visibility = android.view.View.VISIBLE
            } ?: run {
                btnDoorFrontLeft.visibility = android.view.View.GONE
            }
        } ?: run {
            btnDoorFrontLeft.visibility = android.view.View.GONE
        }

        doorFrontLeftTranspEntity?.let {
            val instance = transformManager.getInstance(it)
            val worldTransform = FloatArray(16)
            transformManager.getWorldTransform(instance, worldTransform)
            val pos = floatArrayOf(worldTransform[12], worldTransform[13], worldTransform[14])

            projectWorldToScreen(pos)?.let { (x, y) ->
                btnDoorFrontLeftTransp.x = x - btnDoorFrontLeftTransp.width / 2f
                btnDoorFrontLeftTransp.y = y - btnDoorFrontLeftTransp.height / 2f
                btnDoorFrontLeftTransp.visibility = android.view.View.VISIBLE
            } ?: run {
                btnDoorFrontLeftTransp.visibility = android.view.View.GONE
            }
        } ?: run {
            btnDoorFrontLeftTransp.visibility = android.view.View.GONE
        }


    }

    private fun zoomIn() {
        radius = max(3.5f, radius - 4f) // 최소 거리 제한
        updateCameraWithAngle(currentAngle)
    }

    private fun zoomOut() {
        radius = min(30f, radius + 4f) // 최대 거리 제한
        updateCameraWithAngle(currentAngle)
    }

    private fun turnLeftCameraAngle() {
        animateCameraRotation(currentAngle, currentAngle + 230f)
    }

    private fun turn180CameraAngle() {
        animateCameraRotation(currentAngle, currentAngle - 180f)
    }

    private fun turnRightCameraAngle() {
        animateCameraRotation(currentAngle, currentAngle + 45f)
    }

    private fun animateCameraRotation(from: Float, to: Float) {
        val animator = ValueAnimator.ofFloat(from, to).apply {
            duration = 500
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val animatedAngle = animation.animatedValue as Float
                updateCameraWithAngle(animatedAngle)
            }
            doOnEnd {
                currentAngle = (to % 360 + 360) % 360
                angleDegree = currentAngle.toInt()
            }
        }
        animator.start()
    }

    private fun updateCameraWithAngle(angle: Float) {
        updateCameraWithAngle(angle, currentPitch)
    }

    private fun updateCameraWithAngle(angle: Float, pitch: Float) {
        if (!::camera.isInitialized) return

        val radYaw = Math.toRadians(angle.toDouble())  // 수평 회전
        val radPitch = Math.toRadians(pitch.toDouble())  // 수직 회전

        val eyeX = (radius * cos(radPitch) * cos(radYaw)).toFloat()
        val eyeY = (radius * sin(radPitch)).toFloat()
        val eyeZ = (radius * cos(radPitch) * sin(radYaw)).toFloat()

        val asset = modelViewer.asset
        val center = asset?.boundingBox?.center ?: floatArrayOf(0f, 0f, 0f)

        camera.lookAt(
            eyeX.toDouble(), eyeY.toDouble(), eyeZ.toDouble(),
            center[0].toDouble(), center[1].toDouble(), center[2].toDouble(),
            0.0, 1.0, 0.0
        )
    }

    // Stop rendering animation
    private fun stopAnimationLoop() {
        isAnimationPlaying = false
        if (::choreographer.isInitialized) {
            choreographer.removeFrameCallback(frameCallback)
        }
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }


    private fun destroyFilament() {
        try {
            val engine = modelViewer.engine

            // 1. 애니메이션 루프 중지
            choreographer.removeFrameCallback(frameCallback)

            // 2. 모델 리소스 해제
            modelViewer.asset?.let { asset ->
                val entityManager = EntityManager.get()
                asset.entities.forEach { entity ->
                    engine.destroyEntity(entity)
                    entityManager.destroy(entity)
                }
            }

            // 3. 카메라 해제
            if (::camera.isInitialized) {
                engine.destroyCameraComponent(cameraEntity)
            }

            // 4. 엔진 해제 (마지막에!)
            engine.destroy()
        } catch (e: Exception) {
            Log.w("Filament", "Error while destroying Filament: ${e.message}")
        }
    }


    // Gesture 처리
    // 클래스 내부에 선언
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // 회전 처리
            currentAngle -= distanceX * 0.2f
            currentPitch -= distanceY * 0.1f
            currentPitch = currentPitch.coerceIn(0f, 60f)

            updateCameraWithAngle(currentAngle, currentPitch)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // 더블탭 시 확대/축소 토글 예시
            if (radius > 5f) zoomIn() else zoomOut()
            return true
        }

    }


    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            radius = (radius / scaleFactor).coerceIn(1.5f, 30f)
            updateCameraWithAngle(currentAngle)
            return true
        }
    }
}
