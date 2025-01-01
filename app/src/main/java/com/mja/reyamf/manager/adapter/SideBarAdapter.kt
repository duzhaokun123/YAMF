package com.mja.reyamf.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mja.reyamf.R
import com.mja.reyamf.common.model.AppInfo
import com.mja.reyamf.databinding.SidebarItemviewBinding

class SideBarAdapter (
    private val onClick: (AppInfo) -> Unit,
    private val sideBarApp: ArrayList<AppInfo>,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<SideBarAdapter.ViewHolder>() {

    fun setData(items: List<AppInfo>?) {
        sideBarApp.apply {
            clear()
            items?.let { addAll(it) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sidebar_itemview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(sideBarApp[position])

    override fun getItemCount(): Int = sideBarApp.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val binding = SidebarItemviewBinding.bind(itemView)
        fun bind(appInfo: AppInfo){
            binding.apply {
                ivAppIcon.setImageDrawable(appInfo.icon)
                ivAppIcon.setOnClickListener {
                    onClick(appInfo)
                }
                ivAppIcon.setOnLongClickListener {
                    onLongClick(adapterPosition)
                    true
                }
                if (appInfo.userId == 0) {
                    ivWorkIcon.visibility = View.INVISIBLE
                }
            }
        }
    }

}