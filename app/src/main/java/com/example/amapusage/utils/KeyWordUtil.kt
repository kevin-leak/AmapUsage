package com.example.amapusage.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.example.amapusage.App
import com.example.amapusage.R
import java.util.regex.Matcher
import java.util.regex.Pattern


object KeyWordUtil {

    fun buildKey(text: String, word: String, color: Int): SpannableString {
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
        return spannableString
    }

    fun buildSearchKey(text: String, word: String): SpannableString{
       return buildKey(text, word,  ContextCompat.getColor(App.appContext, R.color.searchKey))
    }

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