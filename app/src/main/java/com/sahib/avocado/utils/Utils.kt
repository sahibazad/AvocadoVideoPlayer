package com.sahib.avocado.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.sahib.avocado.R
import java.io.File

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Activity.startActivityWithFade(intent: Intent) {
    val bundle = ActivityOptionsCompat.makeCustomAnimation(
        this,
        R.anim.fade_in, R.anim.none
    ).toBundle()
    startActivity(intent, bundle)
}

fun Activity.finishWithFade() {
    finish()
    overridePendingTransition(R.anim.none, R.anim.fade_out)
}

fun Context.startActivityWithFade(intent: Intent) {
    val bundle = ActivityOptionsCompat.makeCustomAnimation(
        this,
        R.anim.fade_in, R.anim.none
    ).toBundle()
    startActivity(intent, bundle)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.checkFileExists(uri : Uri) : Boolean {
    var res : Boolean
    val cr = contentResolver;
    val cur = cr.query(Uri.parse(uri.toString()), arrayOf(MediaStore.MediaColumns.DATA), null, null, null);
    if (cur != null) {
        if (cur.moveToFirst()) {
            val filePath = cur.getString(0);
            res = File(filePath).exists();
        } else {
            res = false;
        }
        cur.close();
    } else {
        res = false;
    }
    return res;
}

class Utils {

}