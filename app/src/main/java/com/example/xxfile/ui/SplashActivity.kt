package com.example.xxfile.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xxfile.databinding.ActivitySplashBinding
import com.example.xxfile.ui.main.MainActivity


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(){
    companion object{
        lateinit var binding: ActivitySplashBinding
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }
    private fun initView(){
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MainActivity.startActivity(this)
        finish()
    }


}