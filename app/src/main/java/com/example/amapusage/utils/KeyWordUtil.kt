package com.example.amapusage.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern


object KeyWordUtil {
    /**
     * 关键字高亮变色
     *
     * @param color 变化的色值
     * @param text 文字
     * @param keyword 文字中的关键字
     * @return 结果SpannableString
     */
    fun matcherSearchTitle(color: Int, text: String, word: String): SpannableString {
        val spannableString = SpannableString(text)
        val keyword = escapeExprSpecialWord(word)
        val reText = escapeExprSpecialWord(text)
        if (reText.contains(keyword) && !TextUtils.isEmpty(keyword)) {
            val p: Pattern = Pattern.compile(keyword)
            val m: Matcher = p.matcher(spannableString)
            while (m.find()){
                spannableString.setSpan(
                    ForegroundColorSpan(color),
                    m.start(),
                    m.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        Log.e("KeyWordUtil", "matcherSearchTitle: $spannableString")
        return spannableString
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param keyword
     * @return keyword
     */
    private fun escapeExprSpecialWord(word: String): String {
        var keyword = word
        if (!TextUtils.isEmpty(keyword)) {
            val fbsArr =
                arrayOf("\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|")
            for (key in fbsArr) {
                if (keyword.contains(key)) keyword = keyword.replace(key, "\\" + key)
            }
        }
        return keyword
    }
}