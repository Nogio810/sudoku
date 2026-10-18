package com.example.sudoku.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.R
import com.example.sudoku.domain.model.GameSession

@Composable
fun GameToolbar(session: GameSession, actions: GameActions, modifier: Modifier = Modifier) {
    val cell = session.selectedCell
    val editable = cell != null && session.puzzle.givens[cell] == 0 && !session.isComplete
    val restoresCandidates = editable && session.autoCandidates && session.board.values[cell!!] == 0
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LabeledIconButton(R.drawable.ic_undo, "戻す", "元に戻す", actions.undo,
            Modifier.weight(1f).testTag("undo"), enabled = session.undo.isNotEmpty() && !session.isComplete)
        LabeledIconButton(R.drawable.ic_redo, "進める", "取り消した操作を戻す", actions.redo,
            Modifier.weight(1f).testTag("redo"), enabled = session.redo.isNotEmpty() && !session.isComplete)
        LabeledIconButton(R.drawable.ic_edit_note, "メモ", "メモ入力", actions.toggleNotes,
            Modifier.weight(1f).testTag("notes"), enabled = !session.isComplete,
            active = session.noteMode, toggle = true)
        LabeledIconButton(R.drawable.ic_ink_eraser, "消す",
            if (restoresCandidates) "このマスの候補を戻す" else "このマスの数字とメモを消す", actions.erase,
            Modifier.weight(1f).testTag("erase"), enabled = editable)
    }
}

@Composable
internal fun LabeledIconButton(
    @DrawableRes icon: Int,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    toggle: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    FilledTonalButton(
        onClick = onClick, enabled = enabled,
        modifier = modifier.heightIn(min = 64.dp).semantics {
            contentDescription = description
            if (toggle) {
                toggleableState = if (active) ToggleableState.On else ToggleableState.Off
                stateDescription = if (active) "入" else "切"
            }
        },
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (active) colors.primary else colors.outlineVariant),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (active) colors.primary else Color.Transparent,
            contentColor = if (active) colors.onPrimary else colors.onSurface,
            disabledContainerColor = Color.Transparent,
        ),
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(26.dp))
            Text(label, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics {})
        }
    }
}
