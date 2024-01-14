package com.example.languagesplit

import android.content.Context
import com.google.gson.Gson
import org.intellij.lang.annotations.Language

/**
 * @author chenglei01
 * @date 2024/1/14
 * @time 18:25
 */
object LanguageSP {

    fun saveLanguage(context: Context, languageItem: LanguageItem) {
        context.getSharedPreferences("languageSP", Context.MODE_PRIVATE).edit()
            .putString("language", Gson().toJson(languageItem))
            .apply()
    }

    fun language(context: Context):LanguageItem {
        val saved = context.getSharedPreferences("languageSP", Context.MODE_PRIVATE).getString("language", "")
        return if (saved?.isNotEmpty() == true) {
            Gson().fromJson(saved, LanguageItem::class.java)
        } else {
            LanguageItem("中文", "zh-CN")
        }
    }
}