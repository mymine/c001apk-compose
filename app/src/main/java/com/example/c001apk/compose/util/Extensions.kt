package com.example.c001apk.compose.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.TypedValue
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.example.c001apk.compose.constant.Constants.PREFIX_APP
import com.example.c001apk.compose.constant.Constants.PREFIX_FEED
import com.example.c001apk.compose.constant.Constants.PREFIX_TOPIC
import com.example.c001apk.compose.constant.Constants.PREFIX_USER
import com.example.c001apk.compose.constant.Constants.UTF8
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.regex.Pattern

val Number.dp get() = (toFloat() * Resources.getSystem().displayMetrics.density).toInt()

val Number.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

val String.http2https: String
    get() = if (this.getOrElse(4) { 's' } == 's') this
    else StringBuilder(this).insert(4, 's').toString()

val density = Resources.getSystem().displayMetrics.density
val screenWidth = Resources.getSystem().displayMetrics.widthPixels
val screenHeight = Resources.getSystem().displayMetrics.heightPixels

fun Context.makeToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Modifier.noRippleToggleable(
    value: Boolean,
    onValueChange: (Boolean) -> Unit
): Modifier = composed {
    toggleable(
        value = value,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onValueChange = onValueChange
    )
}

inline fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    crossinline onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

fun Context.copyText(text: String?, showToast: Boolean = true) {
    text?.let {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        ClipData.newPlainText("copy text", it)?.let { clipboardManager.setPrimaryClip(it) }
        if (showToast)
            makeToast("已复制: $it")
    }
}

fun Context.shareText(text: String) {
    val title = "Share"
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(Intent.createChooser(intent, title))
    } catch (e: Exception) {
        makeToast(e.message ?: "failed to share text")
    }
}

enum class ShareType {
    FEED, APP, TOPIC, USER
}

fun getShareText(type: ShareType, id: String): String {
    val prefix = when (type) {
        ShareType.APP -> PREFIX_APP
        ShareType.FEED -> PREFIX_FEED
        ShareType.TOPIC -> PREFIX_TOPIC
        ShareType.USER -> PREFIX_USER
    }
    return "https://www.coolapk1s.com$prefix$id"
}

val String.getAllLinkAndText: String
    get() = if (isEmpty()) "" else
        Pattern.compile("<a class=\"feed-link-url\"\\s+href=\"([^<>\"]*)\"[^<]*[^>]*>")
            .matcher(this).replaceAll(" $1 ")

// onDoubleClick = {} //双击时回调
// onPress = {} //按下时回调
// onLongPress = {} //长按时回调
// onTap = {} //轻触时回调(按下并抬起)
fun Modifier.doubleClick(onDoubleClick: (Offset) -> Unit): Modifier =
    pointerInput(this) {
        detectTapGestures(
            onDoubleTap = onDoubleClick
        )
    }

fun Modifier.longClick(onLongClick: (Offset) -> Unit): Modifier =
    pointerInput(this) {
        detectTapGestures(
            onLongPress = onLongClick
        )
    }

@Composable
inline fun composeClick(
    time: Int = 500,
    crossinline onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(value = 0L) }
    return {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime >= time) {
            onClick()
            lastClickTime = currentTimeMillis
        }
    }
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

val String?.encode: String
    get() = URLEncoder.encode(this?.replace("%", "%25"), UTF8)
val String.decode: String
    get() = URLDecoder.decode(this, UTF8)

fun Context.openInBrowser(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    } catch (e: Exception) {
        makeToast("打开失败")
        e.printStackTrace()
    }
}

