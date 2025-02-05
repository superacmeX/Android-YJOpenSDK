package com.acme.login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.acme.login.R
import com.acme.login.Screen
import com.acme.login.loginInternalNavigate
import com.acme.login.util.LoginLogger
import com.acme.login.vm.LoginViewModel
import com.acme.login.vm.LoginViewModelFactory
import com.superacme.login.view.PhoneSignUpScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class PhoneSignUpFragment : Fragment() {
    private val loginViewModel: LoginViewModel by activityViewModels { LoginViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            id = R.id.login_content_id
            setContent {
                GetContent()
            }
        }
    }

    @Preview
    @Composable
    private fun GetContent() {
        val showLoading = remember {
            mutableStateOf(false)
        }

        // 获取国家码UI状态
//        val viewModel: LoginViewModel = viewModel()
        val userName = loginViewModel.lastNotLoginSuccessNameState.collectAsState().value

        PhoneSignUpScreen(
            userName = userName,
            showProgressBar = showLoading.value,
            currentRegionCode = "zh",
            onValueChange = {
                loginViewModel.changeUserName(it)
            },
            onClearButtonClick = {
                loginViewModel.changeUserName("")
            },
            onEvent = { event ->
                when (event) {
                    is LoginEvent.BackPressed -> findNavController().popBackStack()
                    is LoginEvent.AlertPrivacyNotCheck -> Toasts.showToast(
                        getString(R.string.sm_login_agree_privacy_terms)
                    )

                    is LoginEvent.PhoneOrEmailInvalid -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toasts.showToast(requireContext().getString(R.string.sm_login_error_tips))
                        }
                    }

                    is LoginEvent.VerifySMSCode -> requestSMS(event, showLoading)
                    // 跳转地区选择fragment
                    is LoginEvent.RegionSwitchEvent -> loginInternalNavigate(
                        Screen.RegionSwitch,
                        Screen.PhoneSignUp
                    )

                    else -> {
                        LoginLogger.log(" not handled event $event")
                    }
                }
            })
    }

    protected fun requestSMS(
        event: LoginEvent.VerifySMSCode,
        showLoading: MutableState<Boolean>,
    ) {
        val data = event.args
        showLoading.value = true
        lifecycleScope.launch(Dispatchers.IO) {
            loginViewModel.getSMSCode2(
                data.phone,
                data.phoneArea,
                data.requestType,
                data.email
            ) { success, msg ->
                lifecycleScope.launch(Dispatchers.Main) {
                    showLoading.value = false
                    if (success) {
                        loginInternalNavigate(Screen.SMSCode, Screen.PhoneSignUp, Bundle().also {
                            it.putSerializable(
                                VerifyCodeInputFragment.ARGS_SMS_CODE_COMPLETE,
                                data
                            )
                        })
                    } else {
                        Toasts.showToast(msg)
                    }
                }
            }
        }
    }

}