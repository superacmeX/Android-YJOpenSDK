package com.acme.login.util

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and
import kotlin.text.Charsets.UTF_8

class StringUtil {

    companion object {
        @JvmStatic
        fun splitStringToArrayWithItemLength250(str:String):ArrayList<String> {
            val arrays = arrayListOf<String>()
            var needSplitString = str
            while(needSplitString.length > 250) {
                val append = needSplitString.substring(0,250)
                arrays.add(append)
                needSplitString = needSplitString.substring(250,needSplitString.length)
            }
            arrays.add(needSplitString)
           return arrays
        }



        @JvmStatic
        fun stringToMD5(string: String): String? {
            val hash: ByteArray = try {
                MessageDigest.getInstance("MD5").digest(string.toByteArray(charset("UTF-8")))
            } catch (e: NoSuchAlgorithmException) {
                return null
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                return null
            }
            val hex = StringBuilder(hash.size * 2)
            for (b in hash) {
                if ((b and 0xFF.toByte()) < 0x10) hex.append("0")
                hex.append(Integer.toHexString((b and 0xFF.toByte()).toInt()))
            }
            return hex.toString().lowercase(Locale.getDefault())
        }

        @JvmStatic
        fun stringToMD5v2(string: String): String {
            val byteArray = MessageDigest.getInstance("MD5").digest(string.toByteArray(UTF_8))
            val result = byteArray.joinToString (separator = ""){
                    byte -> "%02x".format(byte)
            }
            return result
        }

        fun saveOneDecimal(num:Float):String {

            return String.format("%.1f",num)
        }


        fun saveTwoDecimalPlaces(num:Float):String {

           return  String.format("%.2f",num)
        }
    }
}

