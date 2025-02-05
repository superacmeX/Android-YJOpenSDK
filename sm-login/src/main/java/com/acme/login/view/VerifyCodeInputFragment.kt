package com.acme.login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.util.LoginLogger
import com.acme.login.vm.LoginState
import com.acme.login.vm.LoginViewModel
import com.acme.login.vm.LoginViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import com.acme.login.R
import com.acme.login.Screen
import com.acme.login.loginInternalNavigate

class VerifyCodeInputFragment : Fragment() {
    companion object {
        const val ARGS_SMS_CODE_COMPLETE = "args_sms_code_complete"
    }

    private val loginViewModel: LoginViewModel by activityViewModels {
        LoginViewModelFactory()
    }

    private var requestType: RequestSMSCodeType = RequestSMSCodeType.LOGIN

    private var verifyCodeInputData: VerifyCodeInputData? = null

    @Volatile
    private var gotoMain: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getSerializable(ARGS_SMS_CODE_COMPLETE)?.let {
            verifyCodeInputData = it as VerifyCodeInputData
            requestType = verifyCodeInputData!!.requestType
        }

        LoginLogger.log("input request type = $requestType")

        lifecycleScope.launch(newSingleThreadContext("SMS_COUNT")) {
            LoginLogger.log("startCountDown")
            loginViewModel.startCountDown().collect()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            id = R.id.login_content_id
            setContent {
                GetVerifyCodeScreen()
            }
        }
    }

    @Composable
    private fun GetVerifyCodeScreen() {
        val data = verifyCodeInputData ?: return
        val smsCountDownState by loginViewModel.loginUIState.collectAsState()
//        LoginLogger.log("smsCountDownState= $smsCountDownState")
        val receiver =
            if (data.phone.isNullOrEmpty()) data.email!! else "${data.phone}"
        val requestKey = loginViewModel.requestCodeKey.collectAsState().value.toString()

        VerifyCodeInputScreen(
            receiver = receiver,
            requestKey = requestKey,
            loginState = smsCountDownState
        ) { event ->
            when (event) {
                is LoginEvent.RetryVerifyCode -> getSMSCode()
                is LoginEvent.BackPressed -> findNavController().popBackStack()
                is LoginEvent.SMSCodeCompleteEvent -> handleSMSCodeComplete(
                    event,
                    smsCountDownState
                )

                else -> {
                    LoginLogger.log("unrecognized event $event")
                }
            }
        }
    }

    private fun handleSMSCodeComplete(
        event: LoginEvent.SMSCodeCompleteEvent,
        loginState: LoginState
    ) {
        val codeInputData = verifyCodeInputData ?: return
        if (loginState.smsChecking) {
            LoginLogger.log("smsChecking ing return")
            return
        }

        loginViewModel.setSMSCheckingState(true)

        lifecycleScope.launch(Dispatchers.IO) {
            LoginLogger.log("handleSMSCodeComplete: begin")
            if (requestType == RequestSMSCodeType.SIGN_UP || requestType == RequestSMSCodeType.FORGETPASS) {
                val result = loginViewModel.checkSMSCode(
                    code = event.code,
                    requestType = requestType.str.toLowerCase(), // 不要用name 服务端判断的是signup 不是sign_up
                    phoneCode = codeInputData.phone,
                    phoneAreaCode = codeInputData.phoneArea,
                    email = codeInputData.email
                )

                loginViewModel.setSMSCheckingState(false)
                if (result) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        loginInternalNavigate(
                            Screen.SET_PWD,
                            Screen.SMSCode,
                            Bundle().also {
                                val setPwdData = codeInputData.copy(code = event.code)
                                it.putSerializable(ARGS_SMS_CODE_COMPLETE, setPwdData)
                            })
                    }
                } else {
                    loginViewModel.resetRequestCodeKey()
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toasts.showToast(requireActivity().getString(R.string.sm_login_code_login_failed))
                    }
                }
            } else if (requestType == RequestSMSCodeType.USERCHANGEBINDSTEPTWO) {

                val result2 = loginViewModel.checkSMSCode(
                    event.code,
                    requestType.str.toLowerCase(), // 不要用name 服务端判断的是signup 不是sign_up
                    codeInputData.phone,
                    codeInputData.phoneArea,
                    codeInputData.email
                )


                if (result2) {
                    val result = loginViewModel.changePhoneOrEmail(
                        codeInputData.phone,
                        codeInputData.phoneArea,
                        codeInputData.email
                    )

                    loginViewModel.setSMSCheckingState(false)
//                    loginViewModel.goToMainPage(requireActivity())
                    requireActivity().finish()

                    lifecycleScope.launch(Dispatchers.Main) {
                        if (result) {
                            Toasts.showToast(requireActivity().getString(R.string.sm_login_set_phone_or_email_success))
                        } else {
                            Toasts.showToast(requireActivity().getString(R.string.sm_login_set_phone_or_email_fail))
                        }
                    }
                } else {
                    loginViewModel.setSMSCheckingState(false)
                    loginViewModel.resetRequestCodeKey()
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toasts.showToast(requireActivity().getString(R.string.sm_login_code_login_failed))
                    }
                }
            } else {

                val (result, showFirstGivenMemberTip) = loginViewModel.loginWithSMSCode(
                    event.code,
                    codeInputData.phone,
                    codeInputData.phoneArea,
                    codeInputData.email
                )
                loginViewModel.setSMSCheckingState(false)
                if (result) {
                    gotoMain = true
                    LoginLogger.log("VerifyCodeInputFragment goToMainPage")
                    loginViewModel.goToMainPage(requireActivity(), showFirstGivenMemberTip)
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toasts.showToast(
                            getString(R.string.sm_login_login_success)
                        )
                    }
                } else {
                    if (!gotoMain) {
                        loginViewModel.resetRequestCodeKey()
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toasts.showToast(
                                getString(R.string.sm_login_code_login_failed)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getSMSCode() {
        val data = verifyCodeInputData ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            loginViewModel.getSMSCode2(
                data.phone,
                data.phoneArea,
                data.requestType,
                data.email
            ) { success, msg ->
                if (success) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        loginViewModel.startCountDown().collect()
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toasts.showToast(msg)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LoginLogger.log("VerifyCodeInputFragment onResume")
    }

    override fun onPause() {
        super.onPause()
        LoginLogger.log("VerifyCodeInputFragment onPause")
    }
}