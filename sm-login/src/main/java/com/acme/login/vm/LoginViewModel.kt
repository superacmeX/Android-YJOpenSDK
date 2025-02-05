package com.acme.login.vm

import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.acme.common.account.login.ILoginBusiness
import com.acme.common.account.login.ILoginCallback
import com.acme.common.account.login.YJLoginBusiness
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.util.LoginLogger
import com.acme.login.view.InnerLoginLogger
import com.alibaba.fastjson.JSONObject
import com.superacme.common_network.constant.NetResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import kotlin.coroutines.resume

const val maxCountTimeSeconds: Int = 59

data class LoginState(
    val countTimeSeconds: Int = maxCountTimeSeconds,
    val smsChecking: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val logTag = "andymao->LoginViewModel${hashCode()}"

    private val viewModelState = MutableStateFlow(LoginState())
    val loginUIState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value
        )


    private val lastUserName = MutableStateFlow("")
    val lastLoginUserName =
        lastUserName.stateIn(viewModelScope, SharingStarted.Eagerly, lastUserName.value)

    private val lastNotLoginSuccessName = MutableStateFlow("")
    val lastNotLoginSuccessNameState =
        lastNotLoginSuccessName.stateIn(viewModelScope, SharingStarted.Eagerly, lastUserName.value)

    var requestCodeKey: MutableStateFlow<Long> = MutableStateFlow(System.currentTimeMillis())

    @Volatile
    var refershed = false

    @Volatile
    var userNameChanged = false

    val loginService: ILoginBusiness? = YJLoginBusiness

    fun refreshUserName() {
        if (!refershed) {
            refershed = true
//            lastUserName.update { loginService.getUserName() ?: "" }
            lastNotLoginSuccessName.update { lastUserName.value }
            InnerLoginLogger.info("$logTag refreshUserName() called ${lastUserName.value}")
        } else {
            InnerLoginLogger.info("$logTag refreshUserName() called skip")
        }
    }

    fun changeUserName(name: String) {
        InnerLoginLogger.info("$logTag changeUserName() called with: name = $name")
        lastNotLoginSuccessName.update { name }
        userNameChanged = true
    }

    suspend fun getSMSCode2(
        phoneNum: String? = null,
        phoneArea: String? = null,
        requestType: RequestSMSCodeType,
        email: String? = null,
        callback: ((Boolean, String) -> Unit)? = null
    ) {
        loginService?.getSMSCode(
            phoneNum = phoneNum,
            phoneArea = phoneArea,
            lang = "zh",
            country = "CN",
            requestType = requestType,
            email = email
        ) { result, msg ->
            callback?.invoke(result, msg ?: "")
        }
    }

    suspend fun register(
        phoneNum: String?,
        email: String?,
        smsCode: String,
        pwdMD5: String,
        requestType: String,
        phoneArea: String?,
        callback: ((Boolean, String) -> Unit)? = null
    ) {
        loginService?.register(
            phoneNum = phoneNum,
            email = email,
            smsCode = smsCode,
            pwdMD5 = pwdMD5,
            requestType = requestType,
            phoneArea = phoneArea
        ) { result, msg ->
            callback?.invoke(result, msg ?: "")
        }
    }

    suspend fun forgetPWD(
        pwd: String,
        smsCode: String,
        requestType: String,
        phoneNum: String?,
        phoneArea: String?,
        email: String?,
        callback: ((Boolean, String?) -> Unit)? = null
    ) {
        loginService?.forgetPWD(
            pwd = pwd,
            smsCode = smsCode,
            requestType = requestType,
            phoneNum = phoneNum,
            phoneArea = phoneArea,
            email = email,
            callback = callback
        )
    }

    fun startCountDown(): Flow<Int> {
        InnerLoginLogger.info("$logTag startCountDown: $maxCountTimeSeconds")
        return (maxCountTimeSeconds downTo 0).asFlow()
            .onEach { delay(1000) }
            .onStart { emit(maxCountTimeSeconds) }
            .conflate()
            .transform { remainingSeconds: Int ->
                viewModelState.update { it.copy(countTimeSeconds = remainingSeconds) }
            }
    }

    suspend fun login(
        name: String,
        pwd: String,
        callback: ((Boolean, Boolean, String?) -> Unit)? = null
    ) {
        loginService?.login(
            userName = name, password = pwd, loginCallback = object : ILoginCallback {
                override fun onLoginSuccess() {
                    callback?.invoke(true, true, null)
                }

                override fun onLoginFailed(
                    response: Response<NetResult<JSONObject>>?,
                    throwable: Throwable?
                ) {
                    callback?.invoke(false, false, null)
                }
            }
        )
    }

    suspend fun loginWithSMSCode(
        code: String,
        phoneCode: String?,
        phoneAreaCode: String?,
        email: String?
    ): Pair<Boolean, Boolean> {
        return suspendCancellableCoroutine {
            loginService?.loginWithSMSCode(
                code = code,
                phoneCode = phoneCode,
                phoneAreaCode = phoneAreaCode,
                email = email
            ) { result, firstLogin, msg ->
                if (result) {
                    it.resume(Pair(true, firstLogin))
                } else {
                    it.resume(Pair(false, false))
                }
            }
        }
    }


    suspend fun changePhoneOrEmail(
        phoneCode: String?,
        phoneAreaCode: String?,
        email: String?
    ): Boolean {

        return false
    }

    suspend fun checkSMSCode(
        code: String,
        requestType: String,
        phoneCode: String?,
        phoneAreaCode: String?,
        email: String?
    ): Boolean {
//        return loginService?.checkSMSCode(code, requestType, phoneCode, phoneAreaCode, email)
//            ?: false

        return suspendCancellableCoroutine<Boolean> {
            loginService?.checkSMSCode(
                code = code,
                requestType = requestType,
                phoneCode = phoneCode,
                phoneAreaCode = phoneAreaCode,
                email = email
            ) { result, msg ->
                if (result) {
                    it.resume(true)
                } else {
                    it.resume(false)
                }
            }
        }
    }

    fun goToMainPage(activity: Activity, showFirstGivenMemberTip: Boolean) {
        LoginLogger.log("goToMainPage goToMainPage")
        val intent = Intent()
        intent.setClassName(activity,"com.superacm.demo.home.HomeActivity")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.application.startActivity(intent)
        activity.finish()
    }

    suspend fun resetPWD(
        pwd1: String,
        pwd2: String,
        callback: ((Boolean, String?) -> Unit)? = null
    ) {
        loginService?.resetPWD(
            oldPwd = pwd1,
            newPwd = pwd2,
            callback = callback
        )
    }

    fun setSMSCheckingState(boolean: Boolean) {
        viewModelState.update { it.copy(smsChecking = boolean) }
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun resetRequestCodeKey() {
        requestCodeKey.value = System.currentTimeMillis()
        InnerLoginLogger.info("$logTag resetRequestCodeKey()")
    }
}

class LoginViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}