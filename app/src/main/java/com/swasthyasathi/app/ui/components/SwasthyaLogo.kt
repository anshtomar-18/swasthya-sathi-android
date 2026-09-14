package com.swasthyasathi.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swasthyasathi.app.R

@Composable
fun SwasthyaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_swasthya_logo),
        contentDescription = "SwasthyaSathi AI Official Logo",
        modifier = modifier.size(size)
    )
}
