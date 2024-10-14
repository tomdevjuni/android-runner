package com.example.run_tracker_native_app.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.example.run_tracker_native_app.R
import java.util.*


object LocaleHelper {

    fun setLocale(c: Context): Context {
        return updateResources(c, getLanguageCode(c))
    }

    private fun getLanguageCode(c: Context): String {
        var languageCode = "en"
        if (Util.getPref(
                c,
                Constant.PREFERENCE_SELECTED_LANGUAGE,
                c.getString(R.string.english)
            ) != ""
        ) {
            when (Util.getPref(
                c,
                Constant.PREFERENCE_SELECTED_LANGUAGE,
                c.getString(R.string.english)
            )) {

                c.getString(R.string.arabic) -> {
                    languageCode = "ar"
                }
                c.getString(R.string.bengali) -> {
                    languageCode = "bn"
                }
                c.getString(R.string.chinese) -> {
                    languageCode = "zh"
                }
                c.getString(R.string.chinese_traditional) -> {
                    languageCode = "zh-rTW"
                }
                c.getString(R.string.english_lang) -> {
                    languageCode = "en"
                }
                c.getString(R.string.french) -> {
                    languageCode = "fr"
                }
                c.getString(R.string.german) -> {
                    languageCode = "de"
                }
                c.getString(R.string.hindi) -> {
                    languageCode = "hi"
                }
                c.getString(R.string.indonesian) -> {
                    languageCode = "id"
                }
                c.getString(R.string.italian) -> {
                    languageCode = "it"
                }
                c.getString(R.string.japanese) -> {
                    languageCode = "ja"
                }
                c.getString(R.string.javanese) -> {
                    languageCode = "jv"
                }
                c.getString(R.string.korean) -> {
                    languageCode = "ko"
                }
                c.getString(R.string.portuguese) -> {
                    languageCode = "pt"
                }
                c.getString(R.string.punjabi) -> {
                    languageCode = "pa"
                }
                c.getString(R.string.russian) -> {
                    languageCode = "ru"
                }
                c.getString(R.string.spanish) -> {
                    languageCode = "es"
                }
                c.getString(R.string.tamil) -> {
                    languageCode = "ta"
                }
                c.getString(R.string.telugu) -> {
                    languageCode = "te"
                }
                c.getString(R.string.turkish) -> {
                    languageCode = "tr"
                }
                c.getString(R.string.urdu) -> {
                    languageCode = "ur"
                }
                c.getString(R.string.vietnamese) -> {
                    languageCode = "vi"
                }

            }
        }
        return languageCode
    }


    private fun updateResources(contextMain: Context, language: String): Context {
        var context: Context = contextMain
        val locale: Locale = if (language == "zh-rTW") {
            Locale("zh", "TW")
        } else {
            Locale(language)
        }
        Locale.setDefault(locale)
        val res: Resources = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
        return context
    }

}