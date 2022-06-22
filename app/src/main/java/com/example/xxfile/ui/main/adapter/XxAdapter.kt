package com.example.xxfile.ui.main.adapter

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.xxfile.ui.main.PicFragment
import com.example.xxfile.ui.main.VideoFragment

class XxAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments: SparseArray<Fragment> = SparseArray()

    init {
        fragments.put(PAGE_SHOOT, PicFragment.newInstance())
        fragments.put(PAGE_VIDEO, VideoFragment.newInstance())
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size()
    }

    fun getFragment(position: Int): Fragment {
        return fragments[position]
    }

    companion object {
        const val PAGE_SHOOT = 0     //拍摄
        const val PAGE_VIDEO = 1     //录制
    }

}