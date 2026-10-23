package it.nicolatomassoni.gandy.ui.devices.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.model.Appliance
import it.nicolatomassoni.gandy.data.model.ApplianceQrData
import kotlinx.serialization.json.Json

private fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@Composable
internal fun ShareQrDialog(
    appliance: Appliance,
    onDismiss: () -> Unit,
) {
    val json = remember(appliance.id) {
        Json.encodeToString(ApplianceQrData.serializer(), ApplianceQrData.from(appliance))
    }
    val qrBitmap = remember(json) { generateQrBitmap(json) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.qr_share_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    appliance.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    appliance.ip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_ok))
            }
        },
    )
}
