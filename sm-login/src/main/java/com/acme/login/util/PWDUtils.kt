package com.acme.login.util

import com.acme.login.view.InnerLoginLogger
import java.util.regex.Pattern

enum class PWDInvalidCode {
    TOO_SHORT,
    TOO_LONG,
    CONTAIN_EMOJI,
    MONO_CATEGORY,
    OK,
}

class PWDUtils {
    companion object {
        private val logTag = "andymao->PWDUtils"

        val lowerBound = 8
        val upperBound = 16

        //数字
        val REG_NUMBER = ".*\\d+.*"

        //大写字母
        val REG_UPPERCASE = ".*[A-Z]+.*"

        //小写字母
        val REG_LOWERCASE = ".*[a-z]+.*"

        //特殊符号(~!@#$%^&*()_+|<>,.?/:;'[]{}\)
        val REG_SYMBOL = ".*[~!@#$%^&*()_+|<>,.?/:;'\\[\\]{}\"]+.*"

        val validPwdReg =
            "^(?![0-9]+\$)(?![a-zA-Z]+\$)(?![\\x21-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e]+\$)[A-Za-z0-9\\x21-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e]{8,16}\$"

        /**
         * 密码规则  在8到16位之间。大小写、数字和特殊字符必须有三类
         * @param password
         * @return
         */
        private fun isLetterDigit(password: String): PWDInvalidCode {
            //密码为空及长度8-16位判断
            if (password.isEmpty() || password.length < lowerBound) return PWDInvalidCode.TOO_SHORT
            if (password.length > upperBound) return PWDInvalidCode.TOO_LONG
            //密码满足数字大小写字母 特殊字符至少三种
            var i = 0
            if (password.matches(Regex(REG_NUMBER))) i++
            if (password.matches(Regex(REG_LOWERCASE))) i++
            if (password.matches(Regex(REG_UPPERCASE))) i++
//            if (password.matches(Regex(REG_SYMBOL))) i++
            return if (i >= 2) {
                PWDInvalidCode.OK
            } else {
                PWDInvalidCode.MONO_CATEGORY
            }
        }

        fun pwdValidV1(input: String): Boolean {
            val code = pwdValidWrapper(input)
            return code == PWDInvalidCode.OK
        }

        fun pwdValid(input: String): Boolean {
            val matches = Pattern.matches(validPwdReg, input)
            return matches
        }

        /**
         * 至少8个字符，至少1个大写字母，1个小写字母和1个数字：
        https://blog.51cto.com/u_15334563/3473108
        ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$
        ^(?=.*[a-z])(?=.*\d)[a-zA-Z\d]{6,}$
         */
        private fun pwdValidWrapper(input: String): PWDInvalidCode {
//    return input.length >= 6 && Pattern.matches("(?=.*\\d)[a-zA-Z\\d]{6,}\$", input)
            //(?=.*\d) 至少有一个数字
//    return input.length >= 6 && Pattern.matches("(?=.*\\d)[a-zA-Z\\d][\\s\\S]{6,}\$", input)

//    return input.length >= 6 && Pattern.matches("[a-z][A-Z][\\d][\\s\\S]{6,}\$", input)
            val result = input.length >= lowerBound && Pattern.matches(
                "[^\\u4e00-\\u9fa5 ]{$lowerBound,$upperBound}\$",
                input
            )
            if (result) {
                val containsEmoji = containsEmoji(input)
                InnerLoginLogger.info("$logTag pwdValid: $containsEmoji")

                if (!containsEmoji) {
                    return isLetterDigit(input)
                } else {
                    return PWDInvalidCode.CONTAIN_EMOJI
                }
            }

            if (input.length > upperBound) {
                return PWDInvalidCode.TOO_LONG
            } else {
                return PWDInvalidCode.TOO_SHORT
            }
//    return input.length >= 6 && Pattern.matches("[^\\u0000-\\uFFFF ]{6,20}\$", input)
//    return input.length >= 6 && Pattern.matches("[a-zA-Z\\d\\S]{6,20}\$", input)
//    return input.length >= 6 && Pattern.matches("[a-zA-Z\\d][\\s\\S]{6,}\$", input)
//    return input.length >= 6 && Pattern.matches(".{6,}\$", input)
        }


        fun containsEmoji(source: String): Boolean {
            val len = source.length
            val isEmoji = false
            for (i in 0 until len) {
                val hs = source[i]
                if (0xd800 <= hs.code && hs.code <= 0xdbff) {
                    if (source.length > 1) {
                        val ls = source[i + 1]
                        val uc = (hs.code - 0xd800) * 0x400 + (ls.code - 0xdc00) + 0x10000
                        if (0x1d000 <= uc && uc <= 0x1f77f) {
                            return true
                        }
                    }
                } else {
                    // non surrogate
                    if (0x2100 <= hs.code && hs.code <= 0x27ff && hs.code != 0x263b) {
                        return true
                    } else if (0x2B05 <= hs.code && hs.code <= 0x2b07) {
                        return true
                    } else if (0x2934 <= hs.code && hs.code <= 0x2935) {
                        return true
                    } else if (0x3297 <= hs.code && hs.code <= 0x3299) {
                        return true
                    } else if (hs.code == 0xa9 || hs.code == 0xae || hs.code == 0x303d || hs.code == 0x3030 || hs.code == 0x2b55 || hs.code == 0x2b1c || hs.code == 0x2b1b || hs.code == 0x2b50 || hs.code == 0x231a) {
                        return true
                    }
                    if (!isEmoji && source.length > 1 && i < source.length - 1) {
                        val ls = source[i + 1]
                        if (ls.code == 0x20e3) {
                            return true
                        }
                    }
                }
            }
            return isEmoji
        }

        fun containsSpecialCharacters(source: String): Int {
            if (source.contains("\\")) {
                return 1
            }

            if (source.contains("\"")) {
                return 2
            }

            if (source.contains("“")) {
                return 3
            }

            if (source.contains("”")) {
                return 4
            }

            return 0
        }

        private fun isEmojiCharacter(codePoint: Char): Boolean {
            return (codePoint.code == 0x0 || codePoint.code == 0x9 || codePoint.code == 0xA || codePoint.code == 0xD
                    || codePoint.code >= 0x20 && codePoint.code <= 0xD7FF || codePoint.code >= 0xE000 && codePoint.code <= 0xFFFD
                    || codePoint.code >= 0x10000 && codePoint.code <= 0x10FFFF)
        }
    }

}

fun main() {
//    val test = DeviceListResponseData()
//    test.deviceName = "d1"
//    println("$test")

    //^\u4e00-\u9fa5
    val testPWDList = listOf<String>(
        "abcdef",
        "1234567",
        "12345",
        "123456789",
        "123456789a",
        "123456789a#&*",
        "Aadefee",
        "汉字圣汉字圣汉字圣",
        "asFF1123$",
        "234#122",
        "#($(#$&(*@#",
        "293421111111113488383838",
        "ASKDJFKKK",
        "acddddd",
        "!i34238",
        " abc bdd12312",
        "廲213dd43a",
        "驪123456aa",
        "\uD866\uDDC3123456qq",
        "21343a",
        "      ",
        "😈123😯mo",
        "ie23*AC22",
        "1234567890abcdefg",
        "1234567890abcdef",
        "12345)G890abcdef",
        "wwwwwwwww@",
        "         ",
        "bcdefeee"
    )

    testPWDList.forEach {
        val pwdValid1 = PWDUtils.pwdValidV1(it)
        val pwdValid = PWDUtils.pwdValid(it)
        println("$it--> pwdValid=$pwdValid")
        assert(pwdValid1 == pwdValid)
    }

//    println(PWDUtils.containsSpecialCharacters("/\""))
//
//    println(PWDUtils.containsSpecialCharacters("/\\"))

//    println(DateUtil.secondToFormatString(40192))
//    println(DateUtil.secondToFormatString(40200))
//    println(DateUtil.secondToFormatString(40208))
//
//    println(DateUtil.secondToFormatString(38123))
}
