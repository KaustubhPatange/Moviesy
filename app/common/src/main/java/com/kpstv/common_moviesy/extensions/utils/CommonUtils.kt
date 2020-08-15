@file:Suppress("MemberVisibilityCanBePrivate")

package com.kpstv.common_moviesy.extensions.utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.DecimalFormat

@Suppress("unused")
object CommonUtils {
    fun getDirSize(dir: File): Long {
        if (dir.exists()) {
            var result: Long = 0
            val fileList: Array<File>? = dir.listFiles()
            for (i in fileList?.indices!!) { // Recursive call if it's a directory
                result += if (fileList[i].isDirectory) {
                    getDirSize(fileList[i])
                } else { // Sum the file size in bytes
                    fileList[i].length()
                }
            }
            return result // return the file size
        }
        return 0
    }

    fun getSizePretty(size: Long?, addPrefix: Boolean = true): String? {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        return if (size != null) {
            when {
                size < sizeMb -> df.format(size / sizeKb)
                    .toString() + if (addPrefix) " KB" else ""
                size < sizeGb -> df.format(
                    size / sizeMb
                ).toString() + " MB"
                size < sizeTerra -> df.format(size / sizeGb)
                    .toString() + if (addPrefix) " GB" else ""
                else -> ""
            }
        } else "0" + if (addPrefix) " B" else ""
    }

    /**
     * Always pass this@Activity as context.
     * Else it won't resolve theme
     */
    fun getColorFromAttr(
        context: Context,
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int = with(context) {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    fun getColoredString(mString: String, colorId: Int): Spannable? {
        val spannable: Spannable = SpannableString(getHtmlText(mString))
        spannable.setSpan(
            ForegroundColorSpan(colorId),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    fun getHtmlText(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(text)
        }
    }

    fun hideKeyboard(context: Context) {
        val imm: InputMethodManager =
            context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}