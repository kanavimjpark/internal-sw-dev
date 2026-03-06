package com.example.a3dmodelsample

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
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a3dmodelsample.retrofit.EtfRepository
import com.example.a3dmodelsample.retrofit.MainViewModelFactory
import com.example.a3dmodelsample.retrofit.NewsRepository
import com.example.a3dmodelsample.retrofit.RetrofitClient
import com.example.a3dmodelsample.retrofit.TickerWsClient
import com.example.a3dmodelsample.retrofit.WeatherRepository
import com.example.a3dmodelsample.retrofit.data.Index
import com.example.a3dmodelsample.viewmodel.MainViewModel
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer
import com.google.android.filament.Camera
import com.google.android.filament.EntityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), MyCarBroadcastReceiver.CommandListener {

    private lateinit var mainViewModel: MainViewModel
    private var filamentStartTime = 0L
    private var firstFrameLogged = false
    // UI components
    private lateinit var surfaceView: SurfaceView
    //for testing stock info
    private lateinit var tvTitle: TextView

    private lateinit var btnMedia: LinearLayout
    private lateinit var btnSetting: ConstraintLayout
    private lateinit var rootLayout: FrameLayout
    private lateinit var btnDoorFrontLeft: ImageButton
    private lateinit var btnDoorFrontRight: ImageButton
    private lateinit var btnDoorRearLeft: ImageButton
    private lateinit var btnDoorRearRight: ImageButton
    private lateinit var btnTrunk: ImageButton

    // Filament ModelViewer & Choreographer
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    private var filamentInitialized = false
    private lateinit var tickerWsClient: TickerWsClient

    //3D
    private lateinit var lvEnter3DSetting: LinearLayout
    private lateinit var lvWidget: LinearLayout
    private lateinit var lv3DSettingView: LinearLayout

    private lateinit var btnHeadLight: Button
    private lateinit var btnAllWindow: Button
    private lateinit var btnSideMirror: Button
    private lateinit var btnFuelDoor: Button

    private var trunkAnimationIndex = listOf<Int>()
    private var multiAnimationIndex = listOf<Int>()
    private enum class Feature { HEADLIGHT, SIDE_MIRROR, FUEL_DOOR }

    private var animationIndex = 0
    private var animationStartTime = 0L
    private var isAnimationPlaying = false
    // 현재 모델 애니메이션이 도는 동안 들어온 "다음 요청" 1개만 보관(마지막 요청 덮어쓰기)
    private var pendingModelAction: (() -> Unit)? = null

    // 종료 콜백 1회 호출 보장용
    private var modelAnimEndFired = false


    private val doorState = mutableMapOf(
        "door_front_left" to false,
        "door_front_right" to false,
        "door_back_left_obj001" to false,
        "door_back_right_obj001" to false,
        "trunk" to false
    )


    private var sideMirrorOpenState = false
    private var fuelDoorOpenState = false
    private var windowOpenState = false
    private var headLightOpenState = false
    private var trunkOpenState = false
//    private var settingState = false
    private var isReversePlaying = false

    //버튼 올리기 위한 변수
    private val entityByName = mutableMapOf<String, Int>()

    // 필요한 엔티티 이름들만 딱 지정
    private val targetEntityNames = setOf(
        "door_front_left",
        "door_back_left_obj001",
        "door_front_right",
        "door_back_right_obj001",
        "trunk"
    )

    private lateinit var camera: Camera
    private var cameraEntity: Int = 0

    private lateinit var cameraController: CameraController
    private lateinit var anchorProjector: AnchorProjector

    private val NEWS_API_KEY = "dd07ab437c704c74babae5f73df37976"
    private val WEATHER_API_KEY = "16b7d1ccd4c3e4f4f42e2051cb5fe5dd"
    private val ALPHA_STOCK_API_KEY = "M19CY7MOHU7ZJ0WP"

    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    // Frame callback for animation update
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(currentTime: Long) {
            choreographer.postFrameCallback(this)

            modelViewer.animator?.takeIf { isAnimationPlaying }?.apply {
                val elapsedSeconds = (System.nanoTime() - animationStartTime) / 1_000_000_000.0f
                val timeInAnim = elapsedSeconds

                modelViewer.animator?.apply {
                    if (trunkAnimationIndex.isNotEmpty()) {
                        val maxDuration = trunkAnimationIndex.maxOfOrNull { getAnimationDuration(it) } ?: 0f

                        for (index in trunkAnimationIndex) {
                            applyAnimation(index, timeInAnim)
                        }

                        if (timeInAnim >= (maxDuration - 0.02f)) {
                            isAnimationPlaying = false
                            updateBoneMatrices()
                        }
                        if (!isAnimationPlaying) {
                            // To run only once
                            if (!modelAnimEndFired) {
                                modelAnimEndFired = true
                                val next = pendingModelAction
                                pendingModelAction = null
                                next?.invoke()
                            }
                        } else {
                            modelAnimEndFired = false
                        }
                    }
                    else if (multiAnimationIndex.isNotEmpty()) {
                        val maxDuration = multiAnimationIndex.maxOfOrNull { getAnimationDuration(it) } ?: 0f

                        for (index in multiAnimationIndex) {
                            applyAnimation(index, timeInAnim)
                        }

                        if (timeInAnim >= (maxDuration - 0.1f)) {
                            isAnimationPlaying = false
                            updateBoneMatrices()
                        } else if (isReversePlaying) {
                            isAnimationPlaying = false
                        }

                        if (!isAnimationPlaying && !isReversePlaying) {
                            // To run only once
                            if (!modelAnimEndFired) {
                                modelAnimEndFired = true
                                val next = pendingModelAction
                                pendingModelAction = null
                                next?.invoke()
                            }
                        } else {
                            if (!modelAnimEndFired) {
                                modelAnimEndFired = true
                                val next = pendingModelAction
                                pendingModelAction = null
                                next?.invoke()
                            }else modelAnimEndFired = false
                        }


                    }
                    else if (animationIndex in 0 until animationCount) {
                        val duration = getAnimationDuration(animationIndex)
                        if (duration > 0f) {
                            val time = timeInAnim.coerceAtMost(duration)

                            applyAnimation(animationIndex, time)

                            if (time >= (duration - 0.02f)) {
                                isAnimationPlaying = false
                            }
                            if (!isAnimationPlaying) {
                                // To run only once
                                if (!modelAnimEndFired) {
                                    modelAnimEndFired = true
                                    val next = pendingModelAction
                                    pendingModelAction = null
                                    next?.invoke()
                                }
                            } else {
                                modelAnimEndFired = false
                            }
                        }
                    }
                    updateBoneMatrices()
                }
            }
            modelViewer.render(currentTime)
            if (!firstFrameLogged) {
                val end = System.currentTimeMillis()
                Log.d("FILAMENT_LOAD", "Filament full load time = ${end - filamentStartTime} ms")
                firstFrameLogged = true
            }
        }
    }

    // Entry point
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        firstFrameLogged = false
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
        MyCarBroadcastReceiver.listener = this
        if (::choreographer.isInitialized) {
            choreographer.postFrameCallback (frameCallback)
        }
    }

    override fun onPause() {
        super.onPause()
        if (MyCarBroadcastReceiver.listener === this) {
            MyCarBroadcastReceiver.listener = null
        }
        stopAnimationLoop()
    }

    override fun onStop() {
        super.onStop()
        tickerWsClient.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyFilament()
    }

    private fun setupViewModel() {
        // Create API clients
        val newsApi = RetrofitClient.createNewsApi(NEWS_API_KEY)
        val etfApi = RetrofitClient.createStockApi(ALPHA_STOCK_API_KEY)

        // Create repositories
        val weatherRepo = WeatherRepository(
            apiKey = WEATHER_API_KEY
        )
        val newsRepo = NewsRepository(
            api = newsApi,
            apiKey = NEWS_API_KEY
        )

        val etfRepo = EtfRepository(
            api = etfApi,
            apiKey = ALPHA_STOCK_API_KEY
        )

        // Create ViewModel via factory
        val factory = MainViewModelFactory(weatherRepo, newsRepo, etfRepo)
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
//                tvTitle.text = "$symbol : $price"
            }
        }

        mainViewModel.loadWeather("Seoul,KR")

//        mainViewModel.loadNews("us")

        mainViewModel.weatherLiveData.observe(this, Observer {weather ->
            Log.d("MainActivity", "🌤 Weather Temp = ${weather.main?.temp}")
        })
//        mainViewModel.newsLiveData.observe(this, Observer { news ->
//            Log.d("MainActivity", "📰 Top Headline = ${news.articles?.firstOrNull()?.title}")
//        })
        mainViewModel.getETFData()
        mainViewModel.etfData.observe(this) { list ->
            Log.d("MainActivity", "ETF size = ${list.size}")
            list.forEach {
                Log.d(
                    "MainActivity",
                    "ETF ${it.displayName} (${it.symbol}) " +
                            "price=${it.currentPrice}, change=${it.change}, valid=${it.isValid}"
                )
            }
        }



        // Bind views
        surfaceView = findViewById(R.id.surfaceView)
        tvTitle = findViewById(R.id.tv_title)
        btnMedia = findViewById(R.id.bt_list)
        btnSetting = findViewById(R.id.manual_widget)
        rootLayout = findViewById(R.id.fl_surface_container)
        lvWidget = findViewById(R.id.lv_widget)
        lv3DSettingView = findViewById(R.id.lv_3d_setting_button)
        btnHeadLight = findViewById(R.id.btn_head_light)
        btnAllWindow = findViewById(R.id.btn_all_window)
        btnSideMirror = findViewById(R.id.btn_side_mirror)
        btnFuelDoor = findViewById(R.id.btn_fuel_door)
        btnDoorFrontLeft = ImageButton(this).apply {
            setBackgroundResource(R.drawable.home_car_door_button)
            setImageResource(R.drawable.door_normal)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(80, 81)
            setOnClickListener {
                runOrQueueModelAction {
                    toggleDoor(
                        id = "door_front_left",
                        btn = this,
                        openAnimIndex = Index.DRIVER_DOOR_OPEN.type,
                        closeAnimIndex = Index.DRIVER_DOOR_CLOSE.type,
                        openIconRes = R.drawable.door_selected,
                        closeIconRes = R.drawable.door_normal
                    )
                }
            }
        }
        btnDoorFrontRight = ImageButton(this).apply {
            setBackgroundResource(R.drawable.home_car_door_button)
            setImageResource(R.drawable.door_normal)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(80, 81)
            setOnClickListener {
                runOrQueueModelAction {
                    toggleDoor(
                        id = "door_front_right",
                        btn = this,
                        openAnimIndex = Index.PASSENGER_DOOR_OPEN.type,
                        closeAnimIndex = Index.PASSENGER_DOOR_CLOSE.type,
                        openIconRes = R.drawable.door_selected,
                        closeIconRes = R.drawable.door_normal
                    )
                }
            }
        }
        btnDoorRearLeft = ImageButton(this).apply {
            setBackgroundResource(R.drawable.home_car_door_button)
            setImageResource(R.drawable.door_normal)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(80, 81)
            setOnClickListener {
                runOrQueueModelAction {
                    toggleDoor(
                        id = "door_back_left_obj001",
                        btn = this,
                        openAnimIndex = Index.REAR_LEFT_DOOR_OPEN.type,
                        closeAnimIndex = Index.REAR_LEFT_DOOR_CLOSE.type,
                        openIconRes = R.drawable.door_selected,
                        closeIconRes = R.drawable.door_normal
                    )
                }
            }
        }
        btnDoorRearRight = ImageButton(this).apply {
            setBackgroundResource(R.drawable.home_car_door_button)
            setImageResource(R.drawable.door_normal)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(80, 81)
            setOnClickListener {
                runOrQueueModelAction {
                    toggleDoor(
                        id = "door_back_right_obj001",
                        btn = this,
                        openAnimIndex = Index.REAR_RIGHT_DOOR_OPEN.type,
                        closeAnimIndex = Index.REAR_RIGHT_DOOR_CLOSE.type,
                        openIconRes = R.drawable.door_selected,
                        closeIconRes = R.drawable.door_normal
                    )
                }
            }
        }
        btnTrunk = ImageButton(this).apply {
            setBackgroundResource(R.drawable.home_car_door_button)
            setImageResource(R.drawable.trunk_normal)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(80, 81)
            setOnClickListener {
                runOrQueueModelAction {
                    if (trunkOpenState) {
                        // 닫기
                        playTrunkAnimations(
                            listOf(
                                Index.TRUNK_CLOSE1.type,
                                Index.TRUNK_CLOSE2.type,
                                Index.TRUNK_CLOSE3.type
                            )
                        )
                        isSelected = false
                        setImageResource(R.drawable.trunk_normal)
                    } else {
                        // 열기
                        playTrunkAnimations(
                            listOf(
                                Index.TRUNK_OPEN1.type,
                                Index.TRUNK_OPEN2.type,
                                Index.TRUNK_OPEN3.type
                            )
                        )
                        isSelected = true
                        setImageResource(R.drawable.trunk_selected)
                    }

                    trunkOpenState = !trunkOpenState
                    Log.d("mjpark", "트렁크 클릭됨 ")
                }
            }
        }
        surfaceView.setZOrderOnTop(false)
        rootLayout.addView(btnDoorFrontLeft)
        rootLayout.addView(btnDoorFrontRight)
        rootLayout.addView(btnDoorRearLeft)
        rootLayout.addView(btnDoorRearRight)
        rootLayout.addView(btnTrunk)

        // Assign button listeners
        btnHeadLight.setOnClickListener {
            Log.d("mjpark","headLightOpenState : $headLightOpenState")
            runOrQueueModelAction {
                if (headLightOpenState) {
                    // off
                    playSeveralAnimations(
                        listOf(
                            Index.HEADLIGHT1.type,
                            Index.HEADLIGHT2.type,
                            Index.HEADLIGHT3.type,
                            Index.HEADLIGHT4.type
                        ),
                        reverse = true
                    )
                } else {
                    // on
                    playSeveralAnimations(
                        listOf(
                            Index.HEADLIGHT1.type,
                            Index.HEADLIGHT2.type,
                            Index.HEADLIGHT3.type,
                            Index.HEADLIGHT4.type
                        ),
                        reverse = false
                    )
                }

                headLightOpenState = !headLightOpenState
                onFeatureButtonClicked(Feature.HEADLIGHT)
            }
        }
        btnAllWindow.setOnClickListener {
            runOrQueueModelAction {
                if (windowOpenState) {
                    // close
                    playSeveralAnimations(
                        listOf(
                            Index.DRIVER_WINDOW_UP.type,
                            Index.PASSENGER_WINDOW_UP.type,
                            Index.REAR_LEFT_WINDOW_UP.type,
                            Index.REAR_RIGHT_WINDOW_UP.type
                        ),
                        reverse = false
                    )
                } else {
                    // open
                    playSeveralAnimations(
                        listOf(
                            Index.DRIVER_WINDOW_DOWN.type,
                            Index.PASSENGER_WINDOW_DOWN.type,
                            Index.REAR_LEFT_WINDOW_DOWN.type,
                            Index.REAR_RIGHT_WINDOW_DOWN.type
                        ),
                        reverse = false
                    )
                }

                windowOpenState = !windowOpenState
            }
        }
        btnSideMirror.setOnClickListener {
            runOrQueueModelAction{
                if (sideMirrorOpenState) {
                    // close
                    playSeveralAnimations(
                        listOf(
                            Index.SIDE_MIRROR_LEFT_CLOSE.type,
                            Index.SIDE_MIRROR_RIGHT_CLOSE.type
                        ),
                        reverse = false
                    )
                } else {
                    // open
                    playSeveralAnimations(
                        listOf(
                            Index.SIDE_MIRROR_LEFT_OPEN.type,
                            Index.SIDE_MIRROR_RIGHT_OPEN.type
                        ),
                        reverse = false
                    )
                }

                sideMirrorOpenState = !sideMirrorOpenState
                onFeatureButtonClicked(Feature.SIDE_MIRROR)
            }
        }
        btnFuelDoor.setOnClickListener {
            runOrQueueModelAction {
                if (fuelDoorOpenState) {
                    // close
                    playAnimation(Index.OIL_DOOR_CLOSE.type)
                } else {
                    // open
                    playAnimation(Index.OIL_DOOR_OPEN.type)
                }

                fuelDoorOpenState = !fuelDoorOpenState
                onFeatureButtonClicked(Feature.FUEL_DOOR)
            }
        }
        lvWidget.visibility = View.VISIBLE
        lv3DSettingView.visibility = View.VISIBLE

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
                Log.d("mjpark", "surfaceCreated")
                initModelViewer() // 이 안에서 Filament 엔진 및 GLB 로딩 시작
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                Log.d("mjpark", "surfaceChanged")
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d("mjpark", "surfaceDestroyed")
            }
        })
    }

    // Initialize ModelViewer and environment
    @SuppressLint("ClickableViewAccessibility")
    private fun initModelViewer() {
        filamentStartTime = System.currentTimeMillis()
        Log.d("FILAMENT_LOAD", "Filament init start")
        if (filamentInitialized) {
            choreographer.postFrameCallback(frameCallback)
            return
        }
        filamentInitialized = true
        choreographer = Choreographer.getInstance()

        modelViewer = ModelViewer(surfaceView)

        gestureDetector = GestureDetector(this, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        cameraEntity = EntityManager.get().create()
        camera = modelViewer.engine.createCamera(cameraEntity)
        camera.setProjection(65.0, 1920.0 / 1080.0, 0.1, 100.0, Camera.Fov.HORIZONTAL) // 종횡비는 화면 비율에 맞게 조정
        modelViewer.view.camera = camera
        surfaceView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            scaleGestureDetector.onTouchEvent(event)
            true
        }

        cameraController = CameraController(camera).apply {
            centerProvider = {
                val c = modelViewer.asset?.boundingBox?.center ?: floatArrayOf(0f, 0f, 0f)

                floatArrayOf(c[0] + 0f, c[1] - 1.0f, c[2] - 1.3f)
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
        val start = System.currentTimeMillis()
        modelViewer.loadModelGlb(buffer)

        val end = System.currentTimeMillis()
        Log.d("3D_LOAD", "GLB load time = ${end - start} ms")
        val asset = modelViewer.asset ?: return
        Log.d("mjpark", "Loaded entities: ${asset.entities.size}")

        entityByName.clear()

        for (entity in asset.entities) {
            val name = asset.getName(entity) ?: continue

            if (name in targetEntityNames) {
                entityByName[name] = entity
                Log.d("mjpark", "Saved entity: $name -> $entity")
            }
        }
        cameraController.setState(
            yaw = 30f,
            pitch = 30f,
            radius = 9.0f
        )
        updateButtonPositions()
    }

    private fun toggleDoor(
        id: String,
        btn: ImageButton,
        openAnimIndex: Int,
        closeAnimIndex: Int,
        openIconRes: Int,
        closeIconRes: Int
    ) {
        val isOpen = doorState[id] == true

        if (isOpen) {
            playAnimation(closeAnimIndex)
            btn.isSelected = false
            btn.setImageResource(closeIconRes)
        } else {
            playAnimation(openAnimIndex)
            btn.isSelected = true
            btn.setImageResource(openIconRes)
        }

        doorState[id] = !isOpen
        Log.d("mjpark", "toggleDoor: $id -> ${doorState[id]}")
    }

    private fun openDoorIfNeeded(
        id: String,
        btn: ImageButton,
        openAnimIndex: Int,
        openIconRes: Int
    ) {
        val isOpen = doorState[id] == true
        if (isOpen) {
            Log.d("MainActivity", "Door already open: $id")
            return
        }

        playAnimation(openAnimIndex)
        btn.isSelected = true
        btn.setImageResource(openIconRes)
        doorState[id] = true

        Log.d("MainActivity", "openDoorIfNeeded: $id -> true")
    }

    private fun closeDoorIfNeeded(
        id: String,
        btn: ImageButton,
        closeAnimIndex: Int,
        closeIconRes: Int
    ) {
        val isOpen = doorState[id] == true
        if (!isOpen) {
            Log.d("MainActivity", "Door already closed: $id")
            return
        }

        playAnimation(closeAnimIndex)
        btn.isSelected = false
        btn.setImageResource(closeIconRes)
        doorState[id] = false

        Log.d("MainActivity", "closeDoorIfNeeded: $id -> false")
    }

    private fun getButtonById(id: String): ImageButton? = when (id) {
        "door_front_left" -> btnDoorFrontLeft
        "door_front_right" -> btnDoorFrontRight
        "door_back_left_obj001" -> btnDoorRearLeft
        "door_back_right_obj001" -> btnDoorRearRight
        "trunk" -> btnTrunk
        else -> null
    }

    private fun handleOpenCommand(position: String?) {
        when (position) {
            "DRIVER" -> openDoorIfNeeded(
                id = "door_front_left",
                btn = btnDoorFrontLeft,
                openAnimIndex = Index.DRIVER_DOOR_OPEN.type,
                openIconRes = R.drawable.door_selected
            )

            "PASSENGER" -> openDoorIfNeeded(
                id = "door_front_right",
                btn = btnDoorFrontRight,
                openAnimIndex = Index.PASSENGER_DOOR_OPEN.type,
                openIconRes = R.drawable.door_selected
            )

            "REAR_LEFT" -> openDoorIfNeeded(
                id = "door_back_left_obj001",
                btn = btnDoorRearLeft,
                openAnimIndex = Index.REAR_LEFT_DOOR_OPEN.type,
                openIconRes = R.drawable.door_selected
            )

            "REAR_RIGHT" -> openDoorIfNeeded(
                id = "door_back_right_obj001",
                btn = btnDoorRearRight,
                openAnimIndex = Index.REAR_RIGHT_DOOR_OPEN.type,
                openIconRes = R.drawable.door_selected
            )

            "ALL" -> {
                handleOpenCommand("DRIVER")
                handleOpenCommand("PASSENGER")
                handleOpenCommand("REAR_LEFT")
                handleOpenCommand("REAR_RIGHT")
            }

            "ALL_WINDOW" -> {
                if (!windowOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.DRIVER_WINDOW_DOWN.type,
                            Index.PASSENGER_WINDOW_DOWN.type,
                            Index.REAR_LEFT_WINDOW_DOWN.type,
                            Index.REAR_RIGHT_WINDOW_DOWN.type
                        ),
                        reverse = false
                    )
                    windowOpenState = true
                }
            }

            "TRUNK" -> {
                if (!trunkOpenState) {
                    playTrunkAnimations(
                        listOf(
                            Index.TRUNK_OPEN1.type,
                            Index.TRUNK_OPEN2.type,
                            Index.TRUNK_OPEN3.type
                        )
                    )
                    trunkOpenState = true
                    btnTrunk.isSelected = true
                    btnTrunk.setImageResource(R.drawable.trunk_selected)
                }
            }

            "OIL" -> {
                if (!fuelDoorOpenState) {
                    playAnimation(Index.OIL_DOOR_OPEN.type)
                    fuelDoorOpenState = true
                }
            }

            "SIDE_MIRROR" -> {
                if (!sideMirrorOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.SIDE_MIRROR_LEFT_OPEN.type,
                            Index.SIDE_MIRROR_RIGHT_OPEN.type
                        ),
                        reverse = false
                    )
                    sideMirrorOpenState = true
                    onFeatureButtonClicked(Feature.SIDE_MIRROR)
                }
            }

            else -> Log.w("MainActivity", "Unhandled OPEN position=$position")
        }
    }


    private fun handleCloseCommand(position: String?) {
        when (position) {
            "DRIVER" -> closeDoorIfNeeded(
                id = "door_front_left",
                btn = btnDoorFrontLeft,
                closeAnimIndex = Index.DRIVER_DOOR_CLOSE.type,
                closeIconRes = R.drawable.door_normal
            )

            "PASSENGER" -> closeDoorIfNeeded(
                id = "door_front_right",
                btn = btnDoorFrontRight,
                closeAnimIndex = Index.PASSENGER_DOOR_CLOSE.type,
                closeIconRes = R.drawable.door_normal
            )

            "REAR_LEFT" -> closeDoorIfNeeded(
                id = "door_back_left_obj001",
                btn = btnDoorRearLeft,
                closeAnimIndex = Index.REAR_LEFT_DOOR_CLOSE.type,
                closeIconRes = R.drawable.door_normal
            )

            "REAR_RIGHT" -> closeDoorIfNeeded(
                id = "door_back_right_obj001",
                btn = btnDoorRearRight,
                closeAnimIndex = Index.REAR_RIGHT_DOOR_CLOSE.type,
                closeIconRes = R.drawable.door_normal
            )

            "ALL" -> {
                handleCloseCommand("DRIVER")
                handleCloseCommand("PASSENGER")
                handleCloseCommand("REAR_LEFT")
                handleCloseCommand("REAR_RIGHT")
            }

            "ALL_WINDOW" -> {
                if (!windowOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.DRIVER_WINDOW_DOWN.type,
                            Index.PASSENGER_WINDOW_DOWN.type,
                            Index.REAR_LEFT_WINDOW_DOWN.type,
                            Index.REAR_RIGHT_WINDOW_DOWN.type
                        ),
                        reverse = false
                    )
                    windowOpenState = true
                }
            }

            "TRUNK" -> {
                if (trunkOpenState) {
                    playTrunkAnimations(
                        listOf(
                            Index.TRUNK_CLOSE1.type,
                            Index.TRUNK_CLOSE2.type,
                            Index.TRUNK_CLOSE3.type
                        )
                    )
                    trunkOpenState = false
                    btnTrunk.isSelected = false
                    btnTrunk.setImageResource(R.drawable.trunk_normal)
                }
            }

            "OIL" -> {
                if (fuelDoorOpenState) {
                    playAnimation(Index.OIL_DOOR_CLOSE.type)
                    fuelDoorOpenState = false
                }
            }

            "SIDE_MIRROR" -> {
                if (sideMirrorOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.SIDE_MIRROR_LEFT_CLOSE.type,
                            Index.SIDE_MIRROR_RIGHT_CLOSE.type
                        ),
                        reverse = false
                    )
                    sideMirrorOpenState = false
                    onFeatureButtonClicked(Feature.SIDE_MIRROR)
                }
            }

            else -> Log.w("MainActivity", "Unhandled CLOSE position=$position")
        }
    }

    private fun handleOnCommand(position: String?) {
        when (position) {
            "HEAD_LIGHT" -> {
                if (!headLightOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.HEADLIGHT1.type,
                            Index.HEADLIGHT2.type,
                            Index.HEADLIGHT3.type,
                            Index.HEADLIGHT4.type
                        ),
                        reverse = false
                    )
                    headLightOpenState = true
                    onFeatureButtonClicked(Feature.HEADLIGHT)
                }
            }
            else -> Log.w("MainActivity", "Unhandled ON position=$position")
        }
    }

    private fun handleOffCommand(position: String?) {
        when (position) {
            "HEAD_LIGHT" -> {
                if (headLightOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.HEADLIGHT1.type,
                            Index.HEADLIGHT2.type,
                            Index.HEADLIGHT3.type,
                            Index.HEADLIGHT4.type
                        ),
                        reverse = true
                    )
                    headLightOpenState = false
                    onFeatureButtonClicked(Feature.HEADLIGHT)
                }
            }
            else -> Log.w("MainActivity", "Unhandled OFF position=$position")
        }
    }

    private fun handleDownCommand(position: String?) {
        when (position) {
            "ALL_WINDOW" -> {
                if (!windowOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.DRIVER_WINDOW_DOWN.type,
                            Index.PASSENGER_WINDOW_DOWN.type,
                            Index.REAR_LEFT_WINDOW_DOWN.type,
                            Index.REAR_RIGHT_WINDOW_DOWN.type
                        ),
                        reverse = false
                    )
                    windowOpenState = true
                }
            }
            else -> Log.w("MainActivity", "Unhandled DOWN position=$position")
        }
    }

    private fun handleUpCommand(position: String?) {
        when (position) {
            "ALL_WINDOW" -> {
                if (windowOpenState) {
                    playSeveralAnimations(
                        listOf(
                            Index.DRIVER_WINDOW_UP.type,
                            Index.PASSENGER_WINDOW_UP.type,
                            Index.REAR_LEFT_WINDOW_UP.type,
                            Index.REAR_RIGHT_WINDOW_UP.type
                        ),
                        reverse = false
                    )
                    windowOpenState = false
                }
            }
            else -> Log.w("MainActivity", "Unhandled UP position=$position")
        }
    }

    private fun handleRotateCommand(position: String?) {
        when (position) {
            "HEAD_LIGHT" -> onFeatureButtonClicked(Feature.HEADLIGHT)
            "SIDE_MIRROR" -> onFeatureButtonClicked(Feature.SIDE_MIRROR)
            "OIL" -> onFeatureButtonClicked(Feature.FUEL_DOOR)
            else -> Log.w("MainActivity", "Unhandled ROTATE position=$position")
        }
    }

    private fun handleZoomInCommand() {
        cameraController.zoomBy(-0.5f)
        updateButtonPositions()
    }

    private fun handleZoomOutCommand() {
        cameraController.zoomBy(0.5f)
        updateButtonPositions()
    }

    private fun updateButtonPositions() {
        val transformManager = modelViewer.engine.transformManager
        val center = cameraController.centerProvider()

        for (name in targetEntityNames) {
            val entity = entityByName[name] ?: continue
            val btn = getButtonById(name) ?: continue
            anchorProjector.updateEntityButton(
                id = name,
                entity = entity,
                btn = btn,
                transformManager = transformManager,
                center = center,
                yOffsetPx = 100f
            )
        }
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
    private fun runOrQueueModelAction(action: () -> Unit) {
        if (isAnimationPlaying) {
            // If it's playing now, wait (leaving only the last request)
            pendingModelAction = action
            return
        }
        action()
    }


    // Play selected animation
    private fun playAnimation(index: Int) {
        val animator = modelViewer.animator ?: return
        if (index < 0 || index >= animator.animationCount) return

        trunkAnimationIndex = emptyList()
        multiAnimationIndex = emptyList()
        isAnimationPlaying = true
        animationIndex = index
        animationStartTime = System.nanoTime()
    }

    private fun playSeveralAnimations(indices: List<Int>, reverse: Boolean = false) {
        val animator = modelViewer.animator ?: return

        if (indices.any { it < 0 || it >= animator.animationCount }) return

        animationIndex = -1
        trunkAnimationIndex = emptyList()
        multiAnimationIndex = indices
        animationStartTime = System.nanoTime()
        isAnimationPlaying = true
        isReversePlaying = reverse
    }

    fun playTrunkAnimations(indices: List<Int>) {
        val animator = modelViewer.animator ?: return

        if (indices.any { it < 0 || it >= animator.animationCount }) return

        animationIndex = -1
        trunkAnimationIndex = indices
        multiAnimationIndex = emptyList()
        animationStartTime = System.nanoTime()
        isAnimationPlaying = true
    }

    private fun onFeatureButtonClicked(feature: Feature) {
        val targetAngle = when (feature) {
            Feature.HEADLIGHT -> 45f
            Feature.SIDE_MIRROR -> 45f
            Feature.FUEL_DOOR -> -45f
        }

        cameraController.animateRotateAndZoom(
            toYaw = targetAngle,
            toRadius = 9.0f,
            durationMs = 500,
            onUpdate = { updateButtonPositions() }
        )
    }

    // Stop rendering animation
    private fun stopAnimationLoop() {
        isAnimationPlaying = false
        isReversePlaying = false
        sideMirrorOpenState = false
        fuelDoorOpenState = false
        windowOpenState = false
        headLightOpenState = false
        trunkOpenState = false
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
            // rotation processing
            cameraController.rotateBy(
                deltaYaw = -distanceX * 0.2f,
                deltaPitch = -distanceY * 0.1f
            )
            updateButtonPositions()
            return true
        }
    }


    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            cameraController.zoomBy(
                deltaRadius = (1f - scale) * 5f
            )
            updateButtonPositions()
            return true
        }
    }

    override fun onCommandReceived(action: String?, position: String?) {
        Log.d("MainActivity", "onCommandReceived action=$action, position=$position")

        if (!::modelViewer.isInitialized) {
            Log.w("MainActivity", "ModelViewer not initialized yet")
            return
        }

        runOnUiThread {
            handleBroadcastCommand(action, position)
        }
    }

    private fun handleBroadcastCommand(action: String?, position: String?) {
        runOrQueueModelAction {
            when (action) {
                MyCarBroadcastReceiver.ACTION_OPEN -> handleOpenCommand(position)
                MyCarBroadcastReceiver.ACTION_CLOSE -> handleCloseCommand(position)
                MyCarBroadcastReceiver.ACTION_ON -> handleOnCommand(position)
                MyCarBroadcastReceiver.ACTION_OFF -> handleOffCommand(position)
                MyCarBroadcastReceiver.ACTION_DOWN -> handleDownCommand(position)
                MyCarBroadcastReceiver.ACTION_UP -> handleUpCommand(position)
                MyCarBroadcastReceiver.ACTION_ROTATE -> handleRotateCommand(position)
                MyCarBroadcastReceiver.ACTION_ZOOMIN -> handleZoomInCommand()
                MyCarBroadcastReceiver.ACTION_ZOOMOUT -> handleZoomOutCommand()
                else -> Log.w("MainActivity", "Unknown action=$action position=$position")
            }
        }
    }
}
