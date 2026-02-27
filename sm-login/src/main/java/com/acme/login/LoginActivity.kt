package com.acme.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.acme.common.account.login.ILoginBusiness
import com.acme.common.account.login.ILoginCallback
import com.acme.common.account.login.YJLoginBusiness
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.util.LoginLogger
import com.acme.login.view.InnerLoginLogger
import com.acme.login.view.Toasts
import com.acme.login.view.VerifyCodeInputData
import com.acme.login.view.VerifyCodeInputFragment
import com.acme.login.view.VerifyCodeLoginFragment.Companion.argumentKeyShowVerifyCodeLoginForActivity
import com.acme.login.vm.LoginViewModel
import com.alibaba.fastjson.JSONObject
import com.google.android.material.navigation.NavigationView
import com.superacme.common_network.constant.NetResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response

interface LoginOnOtherDeviceTips {
    fun show(): Boolean
    fun showed()
}

//@Route(path = "/login/activity?scene=SET_PWD")
class LoginActivity : AppCompatActivity(), LoginOnOtherDeviceTips {
    companion object {
        val setPWD = "SET_PWD"
        val keyScene = "scene"
        val sceneChangePhone = "change_phone_num"
        val sceneChangeEmail = "change_email_num"
        val sceneSignUp = "PhoneSignUp"
        val keyFailCode = "keyFailCode"
    }

    var job: Job? = null

    private val logTag = "andymao->LoginModule->LoginActivity"

    private var sceneName: String? = null
    private var loginFailCode: Int? = null
    private var showed: Boolean = false
    private var navController:NavController? = null
    private val loginService:ILoginBusiness= YJLoginBusiness

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        Toasts.context = this.application
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.sm_login_activity)

        val viewModel = ViewModelProvider(this)[LoginViewModel::class.java]


        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
//        navView.setupWithNavController(navController)
        this.navController = navController
//        sceneName = setPWD
        sceneName = intent.getStringExtra(keyScene)
        sceneName = intent.getStringExtra(keyScene)
        val showVerifyCodeLogin = intent.getBooleanExtra(argumentKeyShowVerifyCodeLoginForActivity, true)
        InnerLoginLogger.info("$logTag onCreate() called with: viewModel = $viewModel, ${viewModel.hashCode()}, sceneName=$sceneName, showVerifyCodeLogin=$showVerifyCodeLogin")
        sceneName = intent.getStringExtra(keyScene)
        if (sceneName == setPWD) {
            navToCheckExists(R.id.set_pwd_fragment, navController)
        } else if (sceneName == sceneSignUp) {
            navToCheckExists(R.id.phone_sign_up_fragment, navController)
        } else if (sceneName == sceneChangePhone || sceneName == sceneChangeEmail) {
            navToCheckExists(R.id.phone_forget_pwd_fragment, navController, Bundle().apply {
                putSerializable(
                    VerifyCodeInputFragment.ARGS_SMS_CODE_COMPLETE,
                    VerifyCodeInputData(
                        requestType = RequestSMSCodeType.USERCHANGEBINDSTEPTWO,
                    )
                )

                putString("scene", sceneName)
            })
        }

        loginFailCode = intent?.getIntExtra(
            keyFailCode, 0 //账号在其他设备登录
        )
//        testVerifyCode(navController)
    }

    override fun onNewIntent(intent:Intent?) {
        super.onNewIntent(intent)
        InnerLoginLogger.info(logTag,"OnNewIntent")
        val data = intent?.getStringExtra("login")
        if(TextUtils.equals(data,"fetchToken") && navController != null) {
            navToCheckExists(R.id.sign_in_fragment, navController!!)
        }
    }

    override fun onResume() {
        super.onResume()
        InnerLoginLogger.info("$logTag onResume: ")
    }

    override fun onStop() {
        super.onStop()
        InnerLoginLogger.info("$logTag onStop: ")
    }

    private fun testVerifyCode(navController: NavController) {
        lifecycleScope.launch(Dispatchers.Main) {
            navController.navigate(R.id.sms_code_fragment, Bundle().apply {
                putSerializable(
                    VerifyCodeInputFragment.ARGS_SMS_CODE_COMPLETE,
                    VerifyCodeInputData(
                        requestType = RequestSMSCodeType.SIGN_UP,
                        email = "andymao@qq.com"
                    )
                )
            })
        }
    }

    private fun processLogin(username: String?, password: String?) {
        job = CoroutineScope(Dispatchers.Main).launch {
            try {
                YJLoginBusiness.login(username!!,password!!,object: ILoginCallback {
                    override fun onLoginSuccess() {
                        LoginLogger.log("loginSuccess")

                    }

                    override fun onLoginFailed(response: Response<NetResult<JSONObject>>?,
                                               throwable:Throwable?) {
                        LoginLogger.log("loginFail")
                    }
                })
            } catch (excep: Exception) {
                Toasts.showToast(excep.message)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        LoginLogger.log("$logTag onDestroy login")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1009 && resultCode == Activity.RESULT_OK) {
            val username = data?.getStringExtra("username")
            val password = data?.getStringExtra("password")
            processLogin(username, password)
        }
    }

    override fun onBackPressed() {
        LoginLogger.log("onBackPressed")
        super.onBackPressed()
        if (sceneName == setPWD || sceneName == sceneChangePhone || sceneName == sceneChangeEmail) {
            finish()
        }
    }

    override fun finish() {
        InnerLoginLogger.info("$logTag finish: ")
        super.finish()
    }

    override fun show(): Boolean {
        return false
    }

    override fun showed() {
        showed = true
    }
}