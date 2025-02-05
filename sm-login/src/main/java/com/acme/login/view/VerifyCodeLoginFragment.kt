package com.acme.login.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.R
import com.acme.login.Screen
import com.acme.login.loginInternalNavigate
import com.acme.login.util.LoginLogger
import com.acme.login.vm.LoginViewModel
import com.acme.login.vm.LoginViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VerifyCodeLoginFragment : Fragment() {

    companion object {
        private const val serialVersionUID = -86L
        const val argumentKeyShowVerifyCodeLoginForFragment = "showVerifyCodeLogin"
        const val argumentKeyShowVerifyCodeLoginForActivity = "showVerifyCodeLoginInt"
        private const val argumentKeyUserName = "userName"
    }

    private var supportOneKey = mutableStateOf(false)
    private var loadingShow = mutableStateOf(false)
    private var showVerifyCodeLogin = true
    private var userNameFromArgument: String? = null
    private val logTag = "andymao->VerifyCodeLoginFragment->"+hashCode()
    private val loginViewModel: LoginViewModel by activityViewModels { LoginViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userNameFromArgument = arguments?.getString(argumentKeyUserName)

        var showVerifyCodeLoginTmp = true
        val showVerifyCodeLoginTmpFromActivity =
            activity?.intent?.extras?.getInt(argumentKeyShowVerifyCodeLoginForActivity, -1)
        val showVerifyCodeLoginFromFragment =
            arguments?.getBoolean(argumentKeyShowVerifyCodeLoginForFragment)
        LoginLogger.log("$logTag onCreate() called with: showVerifyCodeLoginFromFragment=$showVerifyCodeLoginFromFragment, showVerifyCodeLoginTmpFromActivity=$showVerifyCodeLoginTmpFromActivity")
        showVerifyCodeLoginTmp =
            if(showVerifyCodeLoginFromFragment== null && showVerifyCodeLoginTmpFromActivity==null){
                LoginLogger.log("$logTag onCreate() called with: both null , return true")
                true
            }
            else if (showVerifyCodeLoginTmpFromActivity == -1 || showVerifyCodeLoginFromFragment !=null ) {
                LoginLogger.log("$logTag onCreate() called with: showVerifyCodeLoginTmpFromActivity is null default it -1, showVerifyCodeLoginFromFragment=$showVerifyCodeLoginFromFragment")
                showVerifyCodeLoginFromFragment ?: true
            } else {
                LoginLogger.log("$logTag onCreate() called with: showVerifyCodeLoginTmpFromActivity is $showVerifyCodeLoginTmpFromActivity")
//                loginViewModel.setCreateNewSignInFragment(true)
                showVerifyCodeLoginTmpFromActivity == 1
            }

        LoginLogger.log("onCreate() called with: showVerifyCodeLoginTmp = $showVerifyCodeLoginTmp, userName=$userNameFromArgument")
        showVerifyCodeLogin = showVerifyCodeLoginTmp

        lifecycleScope.launch(Dispatchers.IO) {
            supportOneKey.value = false

            loginViewModel.refreshUserName()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        LoginLogger.log("in on createView")
        return ComposeView(requireContext()).apply {
            id = R.id.login_content_id
            setContent {
                val lastInputUserName = loginViewModel.lastNotLoginSuccessNameState.collectAsState().value
                val lastLoginUserName = loginViewModel.lastLoginUserName.collectAsState().value
                LoginLogger.log("onCreateView() called lastInputUserName=$lastInputUserName, lastLoginUserName=$lastLoginUserName" +
                        ", userNameChanged=${loginViewModel.userNameChanged}")
//                val displayingUserName = if(userNameFromArgument!=null){
//                    LoginLogger.log("onCreateView() called return userNameFromArgument=${userNameFromArgument}")
//                    userNameFromArgument ?: ""
//                }else{
//                    if(loginViewModel.userNameChanged){
//                        LoginLogger.log("onCreateView() called return lastInputUserName=${lastInputUserName}")
//                        lastInputUserName
//                    }else{
//                        LoginLogger.log("onCreateView() called return lastLoginUserName=${lastLoginUserName}")
//                        lastLoginUserName
//                    }
//                }

                val displayingUserName = if(loginViewModel.userNameChanged){
                        LoginLogger.log("onCreateView() called return lastInputUserName=${lastInputUserName}")
                        lastInputUserName
                    }else{
                        LoginLogger.log("onCreateView() called return lastLoginUserName=${lastLoginUserName}")
                        lastLoginUserName
                    }

                Box {
                    LoginScreenPage(displayingUserName)
                }
            }
        }
    }

    @Composable
    private fun LoginScreenPage(uname: String) {
        val context = LocalContext.current
        LoginScreen(
            oneKeyLogin = supportOneKey,
            showLoading = loadingShow,
            showVerifyCodeLogin = showVerifyCodeLogin,
            onValueChange = {
                loginViewModel.changeUserName(it)
            },
            onClearButtonClick = {
                loginViewModel.changeUserName("")
            },
            lastUsrName = uname,
            onEvent = { event ->
                when (event) {
                    is LoginEvent.PwdInvalid -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (event.pwd.isEmpty()) {
                                Toasts.showToast(
                                    getString(R.string.sm_login_pwd_can_not_be_empty)
                                )
                            } else {
                                Toasts.showToast(
                                        getString(R.string.sm_login_pwd_mal_formed)
                                )
                            }
                        }
                    }

                    is LoginEvent.AlertPrivacyNotCheck -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toasts.showToast(
                                getString(R.string.sm_login_agree_privacy_terms)
                            )
                        }
                    }

                    is LoginEvent.PhoneOrEmailInvalid -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toasts.showToast(requireContext().getString(R.string.sm_login_error_tips))
                        }
                    }

                    is LoginEvent.PhoneSignUp -> loginInternalNavigate(
                        Screen.PhoneSignUp,
                        Screen.SignIn
                    )

                    is LoginEvent.ForgetPWD -> loginInternalNavigate(
                        Screen.PhoneForgetPWD,
                        Screen.SignIn,
                        bundleOf().also { it.putString("userName", event.name) }
                    )

                    is LoginEvent.GetSMSCodeEvent -> {
                        handleGetVerifyCodeEvent(event)
                    }

                    is LoginEvent.OnKeLogin -> {
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                InnerLoginLogger.info("andymao->loginModule LoginScreenPage() umOneKeyLogin() called back")
                            } catch (ex: Exception) {
                                InnerLoginLogger.error("andymao->LoginModule 3", ex.message)
                            }
                        }
                    }

                    is LoginEvent.GoPWDLogin -> {
                        if (showVerifyCodeLogin) {
                            loginInternalNavigate(
                                to = Screen.PWD_LOGIN,
                                from = Screen.SignIn,
                                args = bundleOf(
                                    Pair(argumentKeyShowVerifyCodeLoginForFragment, false),
                                    Pair(argumentKeyUserName, event.name)
                                )
                            )
                        } else {
                            loginInternalNavigate(to = Screen.SignIn, from = Screen.PWD_LOGIN)
                        }

                    }

                    is LoginEvent.PWDLogin -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (noNetworkWithToast()) {
                                return@launch
                            }

                            if (loadingShow.value) {
                                LoginLogger.log("in login return!!")
                            } else {
                                loadingShow.value = true
                                loginViewModel.login(
                                    event.name,
                                    event.pwd
                                ) { result, showFirstGivenMemberTip,msg ->
                                    loadingShow.value = false

                                    loginViewModel.goToMainPage(requireActivity(), showFirstGivenMemberTip)
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        Toasts.showToast(
                                            getString(R.string.sm_login_login_success)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        LoginLogger.log("can't recognize this event $event")
                    }
                }
            })
    }

    private fun noNetworkWithToast(): Boolean {
//        val networkConnected = WIFIUtil.isNetworkConnected(GlobalProperties.application())
//        if (!networkConnected) {
//            lifecycleScope.launch(Dispatchers.Main) {
//                Toasts.showToast(string(R.string.sm_tf_check_network))
//            }
//        }
//        return !networkConnected
        return false
    }

    private fun handleGetVerifyCodeEvent(event: LoginEvent.GetSMSCodeEvent) {
        val data = if (event.email != null) {
            VerifyCodeInputData(
                requestType = RequestSMSCodeType.LOGIN, email = event.email
            )
        } else {
            VerifyCodeInputData(
                requestType = RequestSMSCodeType.LOGIN,
                phone = event.phoneNum,
                phoneArea = "86"
            )
        }

        loadingShow.value = true
        lifecycleScope.launch(Dispatchers.IO) {
            loginViewModel.getSMSCode2(
                data.phone,
                data.phoneArea,
                data.requestType,
                data.email,
            ) { success, msg ->
                loadingShow.value = false
                lifecycleScope.launch(Dispatchers.Main) {
                    if (success) {
                        loginInternalNavigate(Screen.SMSCode,
                            Screen.SignIn,
                            Bundle().also {
                                it.putSerializable(
                                    VerifyCodeInputFragment.ARGS_SMS_CODE_COMPLETE,
                                    data
                                )
                            })
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toasts.showToast(msg)
                        }
                    }
                }
            }
        }
    }

}