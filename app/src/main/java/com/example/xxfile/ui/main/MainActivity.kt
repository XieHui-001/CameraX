package com.example.xxfile.ui.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ConvertUtils
import com.example.xxfile.databinding.ActivityMainBinding
import com.example.xxfile.magicindicator.TabPagerTitleView
import com.example.xxfile.ui.main.adapter.XxAdapter
import com.gyf.immersionbar.ImmersionBar
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object{
        lateinit var binding : ActivityMainBinding
        @JvmStatic
        fun startActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            if (context is Application) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initImmersionBar()
    }

    private val tabArray by lazy {
       arrayListOf("拍照","录制","OpenGL预览")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initView(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val commonNavigator = CommonNavigator(this)
        commonNavigator.isAdjustMode = true //ture 即标题平分宽度的模式，适用于少量tab的情况
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return tabArray.size
            }

            override fun getTitleView(context: Context?, index: Int): IPagerTitleView? {
                val titleView = TabPagerTitleView(context)
                titleView.text = tabArray[index]
                titleView.textSize = 16.0f  //unit is sp
                titleView.normalColor = this@MainActivity.getColor(android.R.color.darker_gray)
                titleView.selectedColor = this@MainActivity.getColor(android.R.color.black)
                //titleView.setBackgroundColor(if (index % 2 == 0) Color.RED else Color.GREEN)
                titleView.setPadding(ConvertUtils.dp2px(-35.0f), 0, ConvertUtils.dp2px(-15.0f), 0)
                titleView.setOnClickListener {
                    binding.viewPager.currentItem = index
                }
                return titleView
            }

            override fun getIndicator(context: Context?): IPagerIndicator? {
                return null
            }
        }
        binding.magicIndicator.navigator = commonNavigator

        //设置MagicIndicator与ViewPager关联
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                binding.magicIndicator.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding.magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.magicIndicator.onPageSelected(position)
            }
        })
        binding.magicIndicator.setPadding(0,0, ConvertUtils.dp2px(-20.0f),0)
        binding.viewPager.adapter = XxAdapter(this)
        binding.viewPager.offscreenPageLimit = tabArray.size //ViewPager2默认不预加载当前页左右（上下）两页，需要手动设置该属性才会预加载相应页数
        binding.viewPager.isUserInputEnabled = true
        binding.viewPager.setCurrentItem(0, false)
    }

     fun initImmersionBar() {
        ImmersionBar.with(this)
            .statusBarView(binding.includeNavBar.statusBar) //可以为任意view
            .statusBarDarkFont(true, 0.2f)
            .transparentStatusBar()
            .init()
    }

}