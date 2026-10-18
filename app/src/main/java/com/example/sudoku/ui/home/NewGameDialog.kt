package com.example.sudoku.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.sudoku.domain.model.Difficulty

@Composable
fun NewGameDialog(
    difficulty: Difficulty,
    replacesCurrent: Boolean,
    onDismiss: () -> Unit,
    onStart: (Boolean) -> Unit,
) {
    var automatic by rememberSaveable(difficulty.level) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (replacesCurrent) "新しい問題を始めますか？" else "問題を始める") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(difficulty.label, style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.fillMaxWidth().testTag("auto_candidates")
                        .toggleable(automatic, role = Role.Switch, onValueChange = { automatic = it })
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("自動候補", Modifier.weight(1f))
                    Switch(checked = automatic, onCheckedChange = null)
                }
                Text("空きマスの候補を自動で表示します。開始後は変更できません。")
                if (replacesCurrent) Text("いま解いている問題の途中経過は消えます。", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = { onStart(automatic) }, modifier = Modifier.testTag("start_game")) { Text("始める") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("戻る") } },
    )
}
