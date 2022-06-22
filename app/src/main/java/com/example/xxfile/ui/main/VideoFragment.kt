package com.example.xxfile.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface.ROTATION_0
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.xxfile.databinding.ActivityVideoBinding
import com.example.xxfile.ui.base.BaseFragment
import com.example.xxfile.ui.etx.onExecClick
import com.example.xxfile.utils.requestPermission
import com.google.common.util.concurrent.ListenableFuture
import com.gyf.immersionbar.ImmersionBar
import java.io.File
import java.util.*

class VideoFragment : BaseFragment<ActivityVideoBinding>() {

    companion object{
        const val savePath = "/storage/emulated/0/DCIM/Camera/"  // 保存地址
        private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

        fun newInstance(): VideoFragment {
            val arguments = Bundle()
            val fragment = VideoFragment()
            fragment.arguments = arguments
            return fragment
        }

    }

    private var imageCapture: ImageCapture? = null


    override fun getViewBinding() = ActivityVideoBinding.inflate(layoutInflater)


    @RequiresApi(Build.VERSION_CODES.P)
    override fun initData() {}

    @RequiresApi(Build.VERSION_CODES.P)
    override fun initListener() {
        vb.btnPreview.onExecClick {
            showPreview()
        }

        vb.btnStart.onExecClick {
            startRecord()
        }

        vb.btnCloseVideo.onExecClick {
            stopVideo()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initPreview(){
        try {
            mContext.requestPermission(
                Manifest.permission.CAMERA,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) {
                if (it) {
                    cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
                    cameraProviderFuture.addListener(Runnable {
                        val cameraProvider = cameraProviderFuture.get()
                        bindPreview(cameraProvider)
                    }, ContextCompat.getMainExecutor(activity))
                } else {
                    ToastUtils.showShort("权限被拒绝")
                }
            }
        }catch (ex : Exception){
            ToastUtils.showShort("权限获取异常${ex}")
        }
    }
    // 保存录制视频Uri
    var mSaveVideoUris : Uri? = null
    private val mVideoCapture by lazy { VideoCapture.Builder().build() }
    private val preview : Preview = Preview.Builder().build()
    @RequiresApi(Build.VERSION_CODES.P)
    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        // 控制摄像头分辨率
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1920, 1080))
            .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        cameraProvider.unbindAll()
        preview.setSurfaceProvider(vb.previewView.surfaceProvider)


             imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(ROTATION_0) //view.display.rotation
            .build()

       cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview ,imageAnalysis,mVideoCapture)
    }

    @SuppressLint(*["MissingPermission", "RestrictedApi"])
    private fun startRecord()
    {
        if (mVideoCapture != null) {
            val dir = File(savePath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            //创建文件
            val file = File(savePath, "${Date().time}_CameraXVideo.mp4")
            if (file.exists()) {
                file.delete()
            }
            val build: VideoCapture.OutputFileOptions = VideoCapture.OutputFileOptions.Builder(file).build()
            mVideoCapture.startRecording(build, CameraXExecutors.mainThreadExecutor(), @SuppressLint("UnsafeOptInUsageError")
                object :  VideoCapture.OnVideoSavedCallback {
                    /** Called when the video has been successfully saved.  */
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        mSaveVideoUris = outputFileResults.savedUri
                    }

                    /** Called when an error occurs while attempting to save the video.  */
                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                      ToastUtils.showShort("录制错误:${message}")
                    }

                })
        }
    }

    @SuppressLint("RestrictedApi")
    private fun stopVideo() {
        mVideoCapture.stopRecording()
    }

    @SuppressLint("RestrictedApi")
    override fun onDestroy() {
        super.onDestroy()
        if (mVideoCapture!= null){
            mVideoCapture.stopRecording()
        }
    }

    override fun initImmersionBar() {
        ImmersionBar.with(this)
            .statusBarView(vb.includeNavBar.statusBar) //可以为任意view
            .statusBarDarkFont(true, 0.2f)
            .transparentStatusBar()
            .init()
    }

    private fun hidePreview(){
        vb.btnPreview.visibility = View.VISIBLE
        vb.btnStart.visibility = View.GONE
        vb.previewView.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showPreview(){
        vb.btnPreview.visibility = View.GONE
        vb.btnStart.visibility = View.VISIBLE
        vb.previewView.visibility = View.VISIBLE
        initPreview()
    }

    override fun onPause() {
        super.onPause()
        imageCapture = null
        hidePreview()
    }

}