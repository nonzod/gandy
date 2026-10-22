package it.tourtools.gandy.data

import androidx.annotation.StringRes
import it.tourtools.gandy.R
import it.tourtools.gandy.ui.common.UiText
import java.io.IOException

/** Errore di comunicazione con la lavatrice, con messaggio localizzato. */
sealed class CandyException(
    @StringRes val messageRes: Int,
    cause: Throwable? = null,
) : IOException(cause) {
    class EmptyResponse : CandyException(R.string.error_empty_response)
    class NoStatusData : CandyException(R.string.error_no_status)
    class ResponseTooShort : CandyException(R.string.error_response_too_short)
    class KeyRecoveryFailed : CandyException(R.string.error_key_recovery)
    class Timeout(cause: Throwable) : CandyException(R.string.error_timeout, cause)
    class ConnectionRefused(cause: Throwable) : CandyException(R.string.error_connection_refused, cause)
    class UnknownHost(cause: Throwable) : CandyException(R.string.error_unknown_host, cause)
}

/** Messaggio da mostrare per l'eccezione, o null se non ne ha uno. */
fun Throwable.toUiText(): UiText? = when (this) {
    is CandyException -> UiText.Resource(messageRes)
    else -> message?.let(UiText::Dynamic)
}
