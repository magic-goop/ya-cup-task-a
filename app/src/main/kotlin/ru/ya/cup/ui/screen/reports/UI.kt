package ru.ya.cup.ui.screen.reports

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ru.ya.cup.R
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.extension.lazyUi
import ru.ya.cup.ui.common.dialog.DefaultBottomDialog
import ru.ya.cup.ui.common.fragment.BaseFragment
import ru.ya.cup.ui.navigation.Router
import ru.ya.cup.ui.utils.MailSender
import javax.inject.Inject

@AndroidEntryPoint
internal class UI : BaseFragment<State, Action, Nothing, VM>() {

    override val resourceId: Int = R.layout.fragment_reports
    override val vm: VM by viewModels()

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var mailSender: MailSender

    private var dialogDeleteReport: DefaultBottomDialog? = null

    private val adapter: Adapter by lazyUi {
        Adapter(
            getString(R.string.reports_item_report_name_template),
            ::sendReport,
            ::deleteDialog
        ).apply {
            setHasStableIds(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler(view)
        savedInstanceState ?: run { submitAction(Action.Init) }
    }

    override fun onPause() {
        super.onPause()
        dialogDeleteReport?.dismiss()
    }

    private fun initRecycler(view: View) {
        val recycler: RecyclerView = view.findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(requireActivity())
        recycler.adapter = adapter
    }

    override fun render(state: State) {
        super.render(state)
        state.reports?.let { reports ->
            if (reports.isEmpty()) {
                router.back()
            } else {
                adapter.submitList(reports)
            }
        }
    }

    private fun sendReport(report: Report) {
        mailSender.sendMail(report)
    }

    private fun deleteDialog(report: Report) {
        dialogDeleteReport?.cancel()
        dialogDeleteReport = DefaultBottomDialog.Builder()
            .setTitle(R.string.reports_delete_report_title)
            .setMainActionTitle(R.string.reports_delete_report_yes)
            .setSecondaryActionTitle(R.string.reports_delete_report_no)
            .setMainAction {
                submitAction(Action.DeleteReport(report))
            }
            .build(requireContext())
        dialogDeleteReport?.show()
    }
}
