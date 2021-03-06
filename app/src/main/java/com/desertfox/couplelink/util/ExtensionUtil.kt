package com.desertfox.couplelink.util

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.desertfox.couplelink.network.Api
import com.desertfox.couplelink.network.RetrofitProvider
import com.jakewharton.rxbinding3.view.clicks

/**
 * Toast R.String.~~로 접근
 */
fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

/**
 * Toast String 으로 접근
 */
fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

/**
 * ViewClick 구현시 300ms 내에 중복클릭은 제외
 */
fun View.throttleClicks() = this.clicks().throttleFirst(300, java.util.concurrent.TimeUnit.MILLISECONDS)!!

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = true): View {
    return LayoutInflater.from(context).inflate(resource, this, attachToRoot)!!
}

/**
 * sharedPreferences 구현
 */
fun Context.sharedPreferences() = this.getSharedPreferences(COUPLE_LINK, Context.MODE_PRIVATE)!!

val Context.coupleLinkApi: Api
    get() = RetrofitProvider(this).coupleLinkApi

fun Context.spannableString(msg: Any, fontId: Int): SpannableString {
    val massage = if (msg is String) msg else getString(msg as Int)
    return SpannableString(massage).apply {
        setSpan(
                CustomTypefaceSpan(ResourcesCompat.getFont(this@spannableString, fontId)!!),
                0,
                length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }
}