package com.example.sudoku.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/** Nine keys stay in one row; a long press always enters a number without changing memo mode. */
@Composable
fun NumberPad(
    noteMode: Boolean,
    enabled: Boolean,
    onDigit: (Int) -> Unit,
    onNumber: (Int) -> Unit,
    placedCounts: Map<Int, Int> = emptyMap(),
) {
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val digitSize = min(28f, ((maxWidth - 16.dp) / 9).value * 0.72f / LocalDensity.current.fontScale).sp
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (digit in 1..9) {
                val shape = RoundedCornerShape(8.dp)
                val placed = placedCounts[digit] ?: 0
                Surface(
                    modifier = Modifier.weight(1f).heightIn(min = 54.dp).clip(shape)
                        .testTag("digit_$digit")
                        .semantics {
                            contentDescription = "数字$digit"
                            if (placed >= 9) stateDescription = "${placed}個配置済み"
                            if (!enabled) disabled()
                        }
                        .combinedClickable(
                            enabled = enabled, role = Role.Button,
                            onClickLabel = if (noteMode) "候補メモを切り替え" else "数字を入力",
                            onLongClickLabel = "メモではなく数字を入力",
                            onClick = { onDigit(digit) },
                            onLongClick = { onNumber(digit) },
                        ),
                    color = when { !enabled -> colors.onSurface.copy(alpha = 0.08f); placed >= 9 -> colors.surfaceVariant; else -> colors.secondaryContainer },
                    contentColor = when { !enabled -> colors.onSurface.copy(alpha = 0.38f); placed >= 9 -> colors.onSurfaceVariant; else -> colors.onSecondaryContainer },
                    shape = shape,
                ) {
                    Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(digit.toString(), fontSize = digitSize, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
