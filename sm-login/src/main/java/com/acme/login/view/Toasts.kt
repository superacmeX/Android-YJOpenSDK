package com.acme.login.view

import android.content.Context
import android.util.Log
import android.widget.Toast

class Toasts {
    companion object {
        var context:Context? = null
        @JvmStatic
        fun showToast(text:String?) {
            context?.let {
                Log.d("andymao", "showToast() called $text")
                Toast.makeText(it,text,Toast.LENGTH_SHORT).show()
            }
        }

        @JvmStatic
        fun showToast(duration:Long,text:String?) {

        }
    }
}