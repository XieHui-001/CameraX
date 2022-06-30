package com.example.xxfile.ui.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraMetadata.LENS_FACING_BACK
import android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.FocusMeteringAction.*
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.xxfile.databinding.ActivityPicBinding
import com.example.xxfile.ui.etx.onExecClick
import com.example.xxfile.utils.requestPermission
import com.google.common.util.concurrent.ListenableFuture
import com.gyf.immersionbar.ImmersionBar
import java.io.File
import java.util.*
import com.example.xxfile.ui.base.BaseFragment
import com.example.xxfile.utils.hasPermission
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class PicFragment : BaseFragment<ActivityPicBinding>() {
    companion object {
        const val angle0 = Surface.ROTATION_0  // 相机不旋转
        const val angle90 = Surface.ROTATION_90 // 相机旋转90度
        const val angle180 = Surface.ROTATION_180 // 相机旋转180度
        const val angle270 = Surface.ROTATION_270 // 相机旋转270度
        const val CAMERA_FACING_BACK = LENS_FACING_BACK // 相机反面
        const val CAMERA_FACING_Font = LENS_FACING_FRONT // 相机正面
        const val savePath = "/storage/emulated/0/DCIM/Camera/"  // 保存地址
        private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

        fun newInstance(): PicFragment {
            val arguments = Bundle()
            val fragment = PicFragment()
            fragment.arguments = arguments
            return fragment
        }
    }
    private var hasPermission : Boolean = false  // 是否有权限
    private var cameraAngleValue = angle0 // 默认不旋转
    private var cameraViewIsFont = false  // 相机是否是正面  true 是 false 不是
    private var imageCapture: ImageCapture? = null
    private var outPutFile: ImageCapture.OutputFileOptions? = null
    private var processCameraProvider : ProcessCameraProvider? = null
    private var cameraInfo : Camera? = null
    private var cameraSelector: CameraSelector? = null
    @RequiresApi(Build.VERSION_CODES.P)
    override fun initData() {
//        vb.tvActionView.createActionView("test", arrayListOf("测试","测试","测试","测试","测试","测试"),object : ActionView.actionViewBackData{
//            override fun backData(data: String) {
//               ToastUtils.showShort(data)
//            }
//        })
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun initListener() {
        vb.rgType.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                vb.btnAngle0.id ->{resetCameraAngleValue(0)}
                vb.btnAngle90.id ->{resetCameraAngleValue(1)}
                vb.btnAngle180.id ->{resetCameraAngleValue(2)}
                vb.btnAngle270.id ->{resetCameraAngleValue(3)}
            }
        }
        vb.rgChangeCarmeState.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                vb.btnChangeCarmeBack.id ->{
                    if (!cameraViewIsFont){
                        return@setOnCheckedChangeListener
                    }
                    cameraViewIsFont = false
                    showPreview()
                }
                vb.btnChangeCarmeFont.id ->{
                    if (cameraViewIsFont){
                        return@setOnCheckedChangeListener
                    }
                    cameraViewIsFont = true
                    showPreview()
                }
            }
        }

        vb.previewView.setOnTouchListener { _, event ->
            val meteringPoint = vb.previewView.meteringPointFactory
                .createPoint(event.x, event.y)
            automaticFocus(meteringPoint)
            true
        }
        vb.btnPreview.onExecClick {
            showPreview()
        }
        vb.btnPhoto.onExecClick {
            if (imageCapture != null && outPutFile != null) {
                imageCapture!!.takePicture(outPutFile!!,
                    ContextCompat.getMainExecutor(ActivityUtils.getTopActivity()),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(error: ImageCaptureException) {
                            ToastUtils.showShort(error.message)
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val imageSavedPath = outputFileResults.savedUri
                            ToastUtils.showShort(imageSavedPath.toString())
                        }
                    })
            }
        }

        vb.tvRatioValue.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setCameraZoomRatio(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        vb.tvExposureValue.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                changeCameraExposure(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun hidePreview(){
        vb.btnPreview.visibility = View.VISIBLE
        vb.btnPhoto.visibility = View.GONE
        vb.previewView.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showPreview(){
        vb.btnPreview.visibility = View.GONE
        vb.btnPhoto.visibility = View.VISIBLE
        vb.previewView.visibility = View.VISIBLE
        initPreview()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initPreview() {
        if (!hasPermission()) {
            mContext.requestPermission(
                Manifest.permission.CAMERA,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) {
                if (it) {
                    hasPermission = it
                    resetPreView()
                } else {
                    ToastUtils.showShort("权限被拒绝")
                }
            }
        }else{
            resetPreView()
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun resetPreView(){
        getWindowRotationData()
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener(Runnable {
            processCameraProvider?.unbindAll()
            processCameraProvider = cameraProviderFuture.get()
            bindPreview(processCameraProvider!!)
        }, ContextCompat.getMainExecutor(mContext))
    }
    private val preview: Preview = Preview.Builder().build()

    @RequiresApi(Build.VERSION_CODES.P)
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        // 控制摄像头分辨率
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1920, 1080))
            .build()

            cameraSelector = CameraSelector.Builder()
            .requireLensFacing(if (cameraViewIsFont){
                CAMERA_FACING_Font
            }else{
                CAMERA_FACING_BACK
            })
            .build()

        preview.setSurfaceProvider(vb.previewView.surfaceProvider)

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(cameraAngleValue)
            .build()

        if (!exTensionApi(cameraProvider)){
            cameraInfo =  cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector!!, imageCapture, imageAnalysis, preview)
        }

        outPutFile = ImageCapture.OutputFileOptions.Builder(File("$savePath${Date().time}_Test.jpg")).build()
        initRatioAndExposure()
    }

    private fun hasPermission() : Boolean{
        mContext.hasPermission(Manifest.permission.CAMERA,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE){
            hasPermission = it
        }
        return hasPermission
    }


   override fun initImmersionBar() {
        ImmersionBar.with(this)
            .statusBarView(vb.includeNavBar.statusBar) //可以为任意view
            .statusBarDarkFont(true, 0.2f)
            .transparentStatusBar()
            .init()
    }

    override fun onPause() {
        super.onPause()
        imageCapture = null
        outPutFile = null
        processCameraProvider?.unbindAll()
        hidePreview()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun resetCameraAngleValue(id : Int){
        when(id){
            angle0 -> {
                cameraAngleValue = angle0
                showPreview()
            }
            angle90 -> {
                cameraAngleValue = angle90
                showPreview()
            }
            angle180 -> {
                cameraAngleValue = angle180
                showPreview()
            }
            angle270 -> {
                cameraAngleValue = angle270
                showPreview()
            }
        }
    }
    // 获取实时窗口旋转数据
    private fun getWindowRotationData(){
        val orientationEventListener = object : OrientationEventListener(mContext) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                LogUtils.e("窗口旋转数据:${rotation}")
                cameraAngleValue  = rotation
                imageCapture?.targetRotation = rotation
            }
        }
        orientationEventListener.enable()
    }

    private fun setCameraZoomRatio(ratio : Float){
        val minRatio = cameraInfo?.cameraInfo!!.zoomState.value!!.minZoomRatio
        val maxRatio = cameraInfo?.cameraInfo!!.zoomState.value!!.maxZoomRatio
        if (ratio in minRatio..maxRatio){
            cameraInfo?.cameraControl!!.setZoomRatio(ratio)
        }else{
            ToastUtils.showLong("当前变焦值${ratio}已超出")
        }
    }
    // 自动聚焦
    private fun automaticFocus(data : MeteringPoint){
        val action : FocusMeteringAction = FocusMeteringAction.Builder(data,FLAG_AF)
            .addPoint(data, FLAG_AF or FLAG_AE or FLAG_AWB)
            .setAutoCancelDuration(5,TimeUnit.SECONDS)
            .build()
        val cameraControl = cameraInfo!!.cameraControl
        val future = cameraControl.startFocusAndMetering(action)
        LogUtils.e("自动聚焦调用")
    }
    // 修改曝光度
    private fun changeCameraExposure(exposureValue:Int){
        cameraInfo?.cameraControl!!.setExposureCompensationIndex(exposureValue)
            .addListener({
                val currentExposureIndex = cameraInfo?.cameraInfo!!.exposureState.exposureCompensationIndex
                ToastUtils.showShort("当前曝光度${currentExposureIndex}")
            },  ContextCompat.getMainExecutor(mContext))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initRatioAndExposure(){
        val exposureState = cameraInfo?.cameraInfo!!.exposureState

        vb.tvRatioValue.apply {
            visibility = View.VISIBLE
            max = cameraInfo?.cameraInfo!!.zoomState.value!!.maxZoomRatio.toInt()
            min = cameraInfo?.cameraInfo!!.zoomState.value!!.minZoomRatio.toInt()
        }

        vb.tvExposureValue.apply {
            visibility = View.VISIBLE
            isEnabled = exposureState.isExposureCompensationSupported
            max = exposureState.exposureCompensationRange.upper
            min = exposureState.exposureCompensationRange.lower
            progress = exposureState.exposureCompensationIndex
        }
    }

    // 查看是否存在可用扩展接口
    private fun exTensionApi(cameraProvider: ProcessCameraProvider) : Boolean{
        var isExtension = false
        // 创建扩展管理器（使用 Jetpack Concurrent 库）
        val future =  ExtensionsManager.getInstanceAsync(mContext,cameraProvider)
        // Obtain the ExtensionsManager instance from the returned ListenableFuture object
        future.addListener(Runnable() {
                try {
                    val extensionsManager = future.get()
                    // Query if extension is available.
                    if (extensionsManager.isExtensionAvailable(DEFAULT_BACK_CAMERA, ExtensionMode.FACE_RETOUCH)) {
                        // Needs to unbind all use cases before enabling different extension mode.
                        // Retrieve extension enabled camera selector
                        val extensionCameraSelector: CameraSelector = extensionsManager.getExtensionEnabledCameraSelector(DEFAULT_BACK_CAMERA, ExtensionMode.FACE_RETOUCH)
                        // Bind image capture and preview use cases with the extension enabled camera
                        // selector.
                        cameraInfo = cameraProvider.bindToLifecycle(this as LifecycleOwner, extensionCameraSelector, imageCapture, preview)
                        isExtension = true
                    }
                } catch (e: ExecutionException) {
                    LogUtils.e("检查是否存在扩展时发生错误:${e}")
                } catch (e: InterruptedException) {
                    LogUtils.e("检查是否存在扩展时发生错误:${e}")
                }
            }, ContextCompat.getMainExecutor(mContext))
        LogUtils.e(if (isExtension){"存在可用拓展"}else{"不存在可用拓展"})
        return  isExtension
    }

    override fun getViewBinding() = ActivityPicBinding.inflate(layoutInflater)
}