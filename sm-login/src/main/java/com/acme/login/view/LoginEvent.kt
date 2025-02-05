package com.acme.login.view

sealed class LoginEvent {
    data class VerifySMSCode(val args: VerifyCodeInputData) : LoginEvent()
    data class GetSMSCodeEvent(val phoneNum: String? = null, val email: String? = null) :
        LoginEvent()

    data class PWDLogin(val name: String, val pwd: String) : LoginEvent()
    object OnKeLogin : LoginEvent()
    object PhoneSignUp : LoginEvent()
    object AlertPrivacyNotCheck : LoginEvent()
    object PhoneOrEmailInvalid : LoginEvent()
    data class PwdInvalid(val pwd: String) : LoginEvent()
    data class ForgetPWD(val name: String) : LoginEvent()
    data class GoPWDLogin(val name: String) : LoginEvent()
    object BackPressed : LoginEvent()
    data class SetPWD(val pwd1: String, val pwd2: String) : LoginEvent()
    data class ResetPWD(val pwd1: String, val pwd2: String) : LoginEvent()

    object RetryVerifyCode : LoginEvent()
    object VerifyCodeInvalid : LoginEvent()
    data class SMSCodeCompleteEvent(val code: String) : LoginEvent()
    object RegionSwitchEvent : LoginEvent()
}

class LoginSuccessEvent {

}

