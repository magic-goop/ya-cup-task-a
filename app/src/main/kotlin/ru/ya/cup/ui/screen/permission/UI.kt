package ru.ya.cup.ui.screen.permission

import android.os.Build
import android.os.Debug
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.ya.cup.R
import ru.ya.cup.ui.common.fragment.BaseFragment
import ru.ya.cup.ui.navigation.Router
import ru.ya.cup.ui.utils.AnimationHelper
import ru.ya.cup.ui.utils.OnTopResumedListener
import javax.inject.Inject

@AndroidEntryPoint
internal class UI : BaseFragment<State, Action, Nothing, VM>(), OnTopResumedListener {
    private companion object {
        const val EPS: Float = 0.1f
    }

    @Inject
    lateinit var router: Router

    override val resourceId: Int = R.layout.fragment_permission
    override val vm: VM by viewModels()

    private lateinit var llContainer: View
    private lateinit var tvAllow: View
    private var checkRationale: Boolean = false

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                navigateToHome()
            }
            checkRationale = true
        }

    private fun checkPermission() {
        if (isPermissionGranted()) {
            navigateToHome()
        } else {
            view?.run {
                tvAllow = findViewById(R.id.tv_allow)
                llContainer = findViewById(R.id.ll_titles_container)
                tvAllow.setOnClickListener {
                    val rationale = shouldShowRequestPermissionRationale(PERM_NAME)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (rationale) {
                            navigateToSettings()
                        } else {
                            requestPermission.launch(PERM_NAME)
                        }
                    } else {
                        if (checkRationale && !rationale) {
                            navigateToSettings()
                        } else {
                            requestPermission.launch(PERM_NAME)
                        }
                    }
                }
                if (1.0f - tvAllow.alpha > EPS) {
                    AnimationHelper.fadeIn(tvAllow)
                    AnimationHelper.fadeIn(llContainer)
                }
            }
        }
    }

    override fun onResumed() {
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        onResumed()
    }

    private fun navigateToHome() {
        router.navigateToHome()
    }

    private fun navigateToSettings() {
        router.navigateToSettings()
    }
}
