package io.silv.checkers.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.silv.checkers.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

@Composable
fun AnimatedNavIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {

    val scope = rememberCoroutineScope()
    val bumpAnim = remember {
        Animatable(initialValue = 0f)
    }
    val color by animateColorAsState(
        targetValue = if (selected) {
            PrimaryGreen
        } else {
            Color.DarkGray
        }
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            modifier = Modifier.wrapContentHeight(),
            onClick = {
                scope.launch {
                    onClick()
                    bumpAnim.animateTo(-(10f))
                    bumpAnim.animateTo(0f)
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(
                        animateDpAsState(
                            targetValue = if (selected) {
                                52.dp
                            } else {
                                22.dp
                            }
                        ).value
                    )
                    .graphicsLayer {
                        translationY = bumpAnim.value
                    },
                tint = color,
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
        Text(text = text, color = color)
    }
}