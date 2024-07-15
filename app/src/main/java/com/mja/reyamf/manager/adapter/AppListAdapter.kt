package com.mja.reyamf.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mja.reyamf.R
import com.mja.reyamf.common.model.AppInfo
import com.mja.reyamf.databinding.ItemAppBinding

class AppListAdapter (
    private val onClick: (AppInfo) -> Unit,
    private val appList: ArrayList<AppInfo>,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    fun setData(items: List<AppInfo>?) {
        appList.apply {
            clear()
            items?.let { addAll(it) }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(appList[position])

    override fun getItemCount(): Int = appList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val binding = ItemAppBinding.bind(itemView)
        fun bind(appInfo: AppInfo){
            binding.apply {
                val icon = appInfo.icon
                val label = appInfo.label
                ivIcon.setImageDrawable(icon)
                tvLabel.text = label

                ll.setOnClickListener {
                    onClick(appInfo)
                }

                ll.setOnLongClickListener {
                    onLongClick(appInfo)
                    true
                }
            }
        }
    }

}