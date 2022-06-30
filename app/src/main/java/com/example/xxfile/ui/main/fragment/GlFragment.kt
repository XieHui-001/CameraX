package com.example.xxfile.ui.main.fragment

import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.LogUtils
import com.example.xxfile.databinding.FragmentGlViewBinding
import com.example.xxfile.ui.base.BaseFragment
import com.example.xxfile.ui.etx.onExecClick
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.*

class GlFragment : BaseFragment<FragmentGlViewBinding>() {

    companion object {
        const val angle0 = Surface.ROTATION_0  // 相机不旋转
        const val angle90 = Surface.ROTATION_90 // 相机旋转90度
        const val angle180 = Surface.ROTATION_180 // 相机旋转180度
        const val angle270 = Surface.ROTATION_270 // 相机旋转270度
        const val CAMERA_FACING_BACK = CameraMetadata.LENS_FACING_BACK // 相机反面
        const val CAMERA_FACING_Font = CameraMetadata.LENS_FACING_FRONT // 相机正面
        const val savePath = "/storage/emulated/0/DCIM/Camera/"  // 保存地址
        private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

        fun newInstance(): GlFragment {
            val arguments = Bundle()
            val fragment = GlFragment()
            fragment.arguments = arguments
            return fragment
        }
    }
    override fun getViewBinding() = FragmentGlViewBinding.inflate(layoutInflater)

    private var hasPermission : Boolean = false  // 是否有权限
    private var cameraAngleValue = angle0 // 默认不旋转
    private var cameraViewIsFont = false  // 相机是否是正面  true 是 false 不是
    private var imageCapture: ImageCapture? = null
    private var outPutFile: ImageCapture.OutputFileOptions? = null
    private var processCameraProvider : ProcessCameraProvider? = null
    private var cameraInfo : Camera? = null
    private var cameraSelector: CameraSelector? = null

    override fun initData() {}

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

    @RequiresApi(Build.VERSION_CODES.P)
    private fun resetPreView(){
        getWindowRotationData()
        processCameraProvider?.unbindAll()
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener(Runnable {
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
            .requireLensFacing(if (cameraViewIsFont){ CAMERA_FACING_Font }else{ CAMERA_FACING_BACK })
            .build()

        vb.previewView.attachPreview(preview)

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(cameraAngleValue)
            .build()

        cameraInfo =  cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector!!, imageCapture, imageAnalysis, preview)
        outPutFile = ImageCapture.OutputFileOptions.Builder(File("$savePath${Date().time}_Test.jpg")).build()
    }


    override fun initListener() {
        vb.run {
            btnStart.onExecClick {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    resetPreView()
                }
            }
        }
    }
}