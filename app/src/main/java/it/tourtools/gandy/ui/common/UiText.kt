package it.tourtools.gandy.ui.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Testo da mostrare in UI risolto solo al momento della composizione,
 * così ViewModel e modelli non dipendono dal Context.
 */
sealed interface UiText {

    data class Dynamic(val value: String) : UiText

    data class Resource(@StringRes val resId: Int, val args: List<Any>) : UiText {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())
    }

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> stringResource(resId, *args.toTypedArray())
    }

    fun asString(context: Context): String = when (this) {
        is Dynamic -> value
        is Resource -> context.getString(resId, *args.toTypedArray())
    }
}
