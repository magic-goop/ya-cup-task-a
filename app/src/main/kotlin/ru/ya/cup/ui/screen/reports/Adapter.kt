package ru.ya.cup.ui.screen.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.ya.cup.R
import ru.ya.cup.domain.entity.Report
import java.text.SimpleDateFormat
import java.util.*

internal class Adapter(
    private val reportNameTemplate: String,
    private val sendReportCallback: (Report) -> Unit,
    private val deleteReportCallback: (Report) -> Unit
) : ListAdapter<Report, Adapter.VH>(DiffCallback) {
    private val sdf: SimpleDateFormat =
        SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_report, parent, false)
            .let { VH(it, sendReportCallback, deleteReportCallback) }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), reportNameTemplate, sdf)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).timestamp
    }

    class VH(
        view: View,
        sendReportCallback: (Report) -> Unit,
        deleteReportCallback: (Report) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private var report: Report? = null
        private val tvName: TextView = view.findViewById(R.id.tv_report_name)

        init {
            view.findViewById<View>(R.id.tv_send_report).setOnClickListener {
                report?.let { r ->
                    sendReportCallback(r)
                }
            }
            view.findViewById<View>(R.id.tv_delete).setOnClickListener {
                report?.let { r ->
                    deleteReportCallback(r)
                }
            }
        }

        fun bind(report: Report, nameTemplate: String, sdf: SimpleDateFormat) {
            this.report = report
            tvName.text = nameTemplate.format(sdf.format(Date(report.timestamp)))
        }
    }
}

internal object DiffCallback : DiffUtil.ItemCallback<Report>() {
    override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem.id == newItem.id
    }
}
