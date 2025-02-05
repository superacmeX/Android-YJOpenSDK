/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acme.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.acme.common.account.logger.LoginLogger
import java.security.InvalidParameterException

enum class Screen { PhoneSignUp, EmailSignUp, SignIn,PWD_LOGIN, SMSCode, SET_PWD, PhoneForgetPWD, EmailForgetPWD, RegionSwitch }

val navOptions = navOptions {
    anim {
        enter = R.anim.rns_slide_in_from_right
        exit = R.anim.rns_slide_out_to_left
        popEnter = R.anim.rns_slide_in_from_left
        popExit = R.anim.rns_slide_out_to_right
    }
}

fun navToCheckExists(fragmentId: Int, findNavController: NavController, args: Bundle? = null) {
    try {
        val backStackEntry = findNavController.getBackStackEntry(fragmentId)
        findNavController.popBackStack(fragmentId, false)
    } catch (e: Exception) {
        findNavController.navigate(fragmentId, args, navOptions)
    }
}


fun Fragment.loginInternalNavigate(to: Screen, from: Screen, args: Bundle? = null) {
    if (to == from) {
        throw InvalidParameterException("Can't navigate to $to")
    }

    val findNavController = findNavController()

    when (to) {
        Screen.PhoneSignUp -> {
            navToCheckExists(R.id.phone_sign_up_fragment, findNavController, args)
        }

        Screen.SET_PWD -> {
            navToCheckExists(R.id.set_pwd_fragment, findNavController, args)
        }

        Screen.SignIn -> {
            navToCheckExists(R.id.sign_in_fragment, findNavController, args)
        }

        Screen.SMSCode -> {
            findNavController.navigate(R.id.sms_code_fragment, args, navOptions)
        }

        Screen.PhoneForgetPWD -> {
            findNavController.navigate(R.id.phone_forget_pwd_fragment, args, navOptions)
        }

        Screen.PWD_LOGIN -> {
            findNavController.navigate(R.id.pwd_sign_in_fragment2, args, navOptions)
        }

        Screen.RegionSwitch -> {
            findNavController.navigate(R.id.fragment_region_switch, args, navOptions)
        }

        else -> {
            LoginLogger.log("unrecognized $to")
        }
    }
}
