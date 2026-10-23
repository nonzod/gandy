package it.nicolatomassoni.gandy.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** Anteprima in tema chiaro e scuro. */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews
