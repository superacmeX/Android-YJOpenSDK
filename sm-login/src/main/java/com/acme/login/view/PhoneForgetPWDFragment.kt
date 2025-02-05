package com.acme.login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.R
import com.acme.login.Screen
import com.acme.login.loginInternalNavigate
import com.acme.login.util.LoginLogger
import com.acme.login.vm.LoginViewModel
import com.acme.login.vm.LoginViewModelFactory
import com.acme.login.LoginActivity
import com.superacme.login.view.PhoneForgetPWDScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class PhoneForgetPWDFragment : Fragment() {
    private var verifyCodeInputData: VerifyCodeInputData? = null

    private val loginViewModel: LoginViewModel by activityViewModels { LoginViewModelFactory() }

    private var sceneName: String? = null

    private var lastPagePassedUserName: String? = null

    private val logTag = "andymao->PhoneForgetPWDFragment"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(VerifyCodeInputFragment.ARGS_SMS_CODE_COMPLETE)?.let {
            verifyCodeInputData = it as VerifyCodeInputData
        }

        arguments?.getString(LoginActivity.keyScene)?.let {
            sceneName = it
        }

        arguments?.getString("userName")?.let {
            lastPagePassedUserName = it
        }

        LoginLogger.log("$logTag onCreate() called with: lastPagePassedUserName = $lastPagePassedUserName, sceneName=$sceneName")

        lifecycleScope.launch(Dispatchers.IO) {
            loginViewModel.refreshUserName()
        }
    }

    @Preview
    @Composable
    private fun GetContent() {
        val uname by loginViewModel.lastNotLoginSuccessNameState.collectAsState()
        val userName = lastPagePassedUserName ?: uname
        LoginLogger.log("$logTag GetContent() called userName=$userName")

        PhoneForgetPWDScreen(
            requestType = verifyCodeInputData?.requestType ?: RequestSMSCodeType.FORGETPASS,
            scene = sceneName,
            lastUsrName = userName,
            onValueChange = {
                loginViewModel.changeUserName(it)
            },
            onClearButtonClick = {
                loginViewModel.changeUserName("")
            },
            onEvent = { event ->
                if (event is LoginEvent.VerifySMSCode) {
                    requestSMS(event)
                }

                if (event is LoginEvent.BackPressed) {
                    findNavController().popBackStack()
                }

                if (event is LoginEvent.PhoneOrEmailInvalid) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toasts.showToast(requireContext().getString(R.string.sm_login_error_tips))
                    }
                }
            })
    }

    protected fun requestSMS(
        event: LoginEvent.VerifySMSCode,
    ) {
        val data = event.args
        lifecycleScope.launch(Dispatchers.IO) {
            loginViewModel.getSMSCode2(
                data.phone,
                data.phoneArea,
                data.requestType,
                data.email
            ) { success, msg ->
                lifecycleScope.launch(Dispatchers.Main) {
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