package com.superacme.login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.acme.common.account.login.ILoginCallback
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.R
import com.acme.login.Screen
import com.acme.login.loginInternalNavigate
import com.acme.login.util.LoginLogger
import com.acme.login.util.PWDUtils
import com.acme.login.util.StringUtil
import com.acme.login.view.LoginEvent
import com.acme.login.view.SetPWDScreen
import com.acme.login.view.Toasts
import com.acme.login.view.VerifyCodeInputData
import com.acme.login.view.VerifyCodeInputFragment
import com.acme.login.view.VerifyCodeLoginFragment.Companion.argumentKeyShowVerifyCodeLoginForActivity

import com.acme.login.vm.LoginViewModel
import com.acme.login.vm.LoginViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetPWDFragment : Fragment() {
    private var loadingShow = mutableStateOf(false)

    private val loginViewModel: LoginViewModel by activityViewModels { LoginViewModelFactory() }

    private var verifyCodeInputData: VerifyCodeInputData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            val requestType = verifyCodeInputData?.requestType ?: RequestSMSCodeType.RESET_PWD
            setContent {
                SetPWDScreen(requestType) { event ->
                    when (event) {
                        is LoginEvent.SetPWD -> {
                            handleSetPWD(event)
                        }

                        is LoginEvent.ResetPWD -> {
                            handleResetPWD(event)
                        }

                        is LoginEvent.BackPressed -> {
                            if (requestType == RequestSMSCodeType.RESET_PWD) {
                                activity?.finish()
                            } else {
                                findNavController().popBackStack()
                            }
                        }

                        else -> {
                            LoginLogger.log(" not handled event $event")
                        }
                    }
                }
            }
        }
    }

    private fun handleResetPWD(event: LoginEvent.ResetPWD) {
        if (event.pwd2.length < 6) {
            val errorTip =
                getString(R.string.sm_login_pwd_length_should_greater_eight)
            Toasts.showToast(errorTip)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            loginViewModel.resetPWD(
                StringUtil.stringToMD5v2(event.pwd1),
                StringUtil.stringToMD5v2(event.pwd2),
                callback = { result, msg ->
                    LoginLogger.log("handleResetPWD resetPWD back $result, $msg")
                    if (result) {
                        safeShowToast(R.string.sm_login_reset_pwd_success)
                        lifecycleScope.launch(Dispatchers.IO) {
                            LoginLogger.log("handleResetPWD logout")

                        }
                    } else {
                        if (msg.isNullOrEmpty()) {
                            safeShowToast(R.string.sm_login_reset_pwd_fail)
                        } else {
                            safeShowToast(msg)
                        }
                    }
                }
            )
        }
    }

    private fun safeShowToast(msg: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toasts.showToast(msg)
        }
    }

    private fun safeShowToast(msg: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
        }
    }

    private fun handleSetPWD(it: LoginEvent.SetPWD): Boolean {
        val pwdText = it.pwd1
        if (it.pwd1.length < PWDUtils.lowerBound) {
            val errorTip =
                requireActivity().getString(R.string.sm_login_pwd_length_should_greater_eight)
            Toasts.showToast(errorTip)
            return true
        }

        if (it.pwd1 != it.pwd2) {
            val errorTip =
                requireActivity().getString(R.string.sm_login_set_pwd_two_pwd_different)
            Toasts.showToast(errorTip)
            return true
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val verifyCodeInputData = verifyCodeInputData ?: return@launch
            val smsCode = verifyCodeInputData.code ?: return@launch
            val phoneNum = verifyCodeInputData.phone
            val uname = verifyCodeInputData.phone ?: verifyCodeInputData.email!!
            val pwdMD5 = StringUtil.stringToMD5v2(pwdText)!!

            if (verifyCodeInputData.requestType == RequestSMSCodeType.SIGN_UP) {
                loginViewModel.register(
                    phoneNum,
                    verifyCodeInputData.email,
                    smsCode,
                    pwdMD5,
                    verifyCodeInputData.requestType.str.lowercase(),
                    verifyCodeInputData.phoneArea
                ) { result, msg ->
                    if (result) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            loginViewModel.login(
                                uname,
                                pwdText
                            ) { result, firstLogin, msg ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if(result){
                                        requireActivity().finish()
                                        Toasts.showToast(getString(R.string.sm_login_login_success))
                                    }else{
                                        Toasts.showToast(getString(R.string.sm_login_code_login_failed))
                                    }
                                }
                            }
                        }

                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toasts.showToast(msg)
                        }
                    }
                }
            } else if (verifyCodeInputData.requestType == RequestSMSCodeType.FORGETPASS) {
                if (loadingShow.value) {
                    LoginLogger.log("return when forget pass, in loading")
                } else {
                    loadingShow.value = true
                    val pwdMD5 = StringUtil.stringToMD5v2(pwdText)!!
                    loginViewModel.forgetPWD(
                        pwd = pwdMD5,
                        smsCode = smsCode,
                        requestType = verifyCodeInputData.requestType.str.lowercase(),
                        phoneNum = verifyCodeInputData.phone,
                        phoneArea = verifyCodeInputData.phoneArea,
                        email = verifyCodeInputData.email,
                    ) { result, msg ->
                        if (result) {
                            loadingShow.value = false
//                            LoginUtil.quitAPP()
//                            Logger.info("forgetPWD() quitAPP")
                            LoginLogger.log("handleSetPWD() forgetPWD server call success")
                            lifecycleScope.launch(Dispatchers.Main) {
                                LoginLogger.log("handleSetPWD() forgetPWD server call success in main")
                                loginInternalNavigate(
                                    to = Screen.PWD_LOGIN,
                                    from = Screen.SET_PWD,
                                    args = bundleOf(
                                        Pair(argumentKeyShowVerifyCodeLoginForActivity, false),
                                    )
                                )
                            }
//                            lifecycleScope.launch(Dispatchers.IO) {
//                                loginViewModel.login(
//                                    uname,
//                                    pwdText
//                                ) { loginResult, msg, showFirstGivenMemberTip ->
//                                    LoginLogger.log("goToMainPage in set pwd")
//                                    loadingShow.value = false
//                                    val act = activity ?: ARouter.getInstance()
//                                        .navigation(ActivityService::class.java).getTopActivity()
//                                    if (loginResult && act != null) {
//                                        loginViewModel.goToMainPage(act, showFirstGivenMemberTip)
//                                    } else {
//                                        lifecycleScope.launch(Dispatchers.Main) {
//                                            Toasts.showToast(msg)
//                                        }
//                                    }
//                                }
//                            }

                        } else {
                            loadingShow.value = false
                            lifecycleScope.launch(Dispatchers.Main) {
                                Toasts.showToast(msg)
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(VerifyCodeInputFragment.ARGS_SMS_CODE_COMPLETE)?.let {
            verifyCodeInputData = it as VerifyCodeInputData
        }
    }
}