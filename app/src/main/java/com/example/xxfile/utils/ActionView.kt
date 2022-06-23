package com.example.xxfile.utils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xxfile.R
import com.example.xxfile.databinding.ItemActionViewBinding
import com.example.xxfile.ui.base.BaseAdapter
import com.example.xxfile.ui.etx.onExecClick

class ActionView: LinearLayout {

    private var titleText : TextView? = null
    private var recyclerView : RecyclerView?  = null
    private var actionListener: actionViewBackData? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        initView()
        initData()
    }
    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.activity_action_view, this)
        titleText = findViewById(R.id.tv_title)
        recyclerView = findViewById(R.id.recycler_view)
    }
    private fun initData(){
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter
    }

    fun createActionView(title : String,list : ArrayList<String>, actionListener: actionViewBackData? = null){
        titleText?.text = title
        adapter.refreshData(list)
        this.actionListener = actionListener
    }

    private val adapter by lazy { ActionAdapter(context,list,object : ActionAdapter.actionViewAdapterItemData{
        override fun getData(str: String) {
            actionListener?.backData(str)
        }
    })
    }
    private val list : ArrayList<String> by lazy { arrayListOf() }
    class ActionAdapter(context: Context,data : ArrayList<String>,private val actionListener: actionViewAdapterItemData? = null) : BaseAdapter<ItemActionViewBinding,String>(context,data){
        override fun convert(vb: ItemActionViewBinding, t: String, position: Int) {
            vb.tvItem.text = t
            vb.tvItem.onExecClick {
                actionListener?.getData(t)
            }
        }

        interface actionViewAdapterItemData{
            fun getData(str : String)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    interface actionViewBackData{
        fun backData(data : String)
    }
}