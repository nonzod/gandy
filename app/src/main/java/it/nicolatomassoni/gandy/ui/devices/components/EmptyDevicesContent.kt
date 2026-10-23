package it.nicolatomassoni.gandy.ui.devices.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.nicolatomassoni.gandy.R

@Composable
internal fun EmptyDevicesContent(
    onAdd: () -> Unit,
    onProvision: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = 40.dp, y = (-30).dp),
            ) {
                drawCircle(
                    color = colors.primary,
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = 0.15f,
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .offset(x = (-60).dp, y = 50.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.2f)),
            )
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(R.drawable.ill_device_empty),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.devices_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.devices_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.outline,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
        ) {
            Text(stringResource(R.string.devices_add))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onProvision,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
        ) {
            Icon(Icons.Outlined.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.devices_provision_empty))
        }
    }
}
