package com.conreo.couchytv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.conreo.couchytv.R
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.conreo.couchytv.data.AppEntry

data class MenuEntry(
    val label: String,
    val icon: ImageVector,
    val action: () -> Unit,
)

/** Long-press menu: open, move, hide, app info, close, uninstall. */
@Composable
fun AppContextMenu(
    app: AppEntry,
    isHidden: Boolean,
    isInput: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onMove: () -> Unit,
    onToggleHide: () -> Unit,
    onAppInfo: () -> Unit,
    onClose: () -> Unit,
    onUninstall: () -> Unit,
    onChangeIcon: () -> Unit,
) {
    val common = listOf(
        MenuEntry(stringResource(R.string.menu_open), AppIcons.Play, onOpen),
        MenuEntry(stringResource(R.string.menu_move), AppIcons.Move, onMove),
        MenuEntry(if (isHidden) stringResource(R.string.menu_unhide) else stringResource(R.string.menu_hide), AppIcons.Hide, onToggleHide),
    )
    // Inputs aren't packages: no app info / close / uninstall, but the icon is choosable.
    val entries = if (isInput) common + MenuEntry(
        stringResource(R.string.menu_change_icon), app.glyph ?: AppIcons.Hdmi, onChangeIcon,
    ) else common + listOf(
        MenuEntry(stringResource(R.string.menu_app_info), AppIcons.Info, onAppInfo),
        MenuEntry(stringResource(R.string.menu_close), AppIcons.Stop, onClose),
        MenuEntry(stringResource(R.string.menu_uninstall), AppIcons.Delete, onUninstall),
    )
    MenuDialog(title = app.label, entries = entries, onDismiss = onDismiss)
}

/** Quick input switcher opened from the status bar. */
@Composable
fun InputPicker(
    inputs: List<AppEntry>,
    onPick: (AppEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    MenuDialog(
        title = stringResource(R.string.cat_inputs),
        entries = inputs.map { MenuEntry(it.label, it.glyph ?: AppIcons.Hdmi) { onPick(it) } },
        onDismiss = onDismiss,
        // Opened by a click, not a long-press: nothing to wait out.
        awaitRelease = false,
    )
}

@Composable
private fun MenuDialog(
    title: String,
    entries: List<MenuEntry>,
    onDismiss: () -> Unit,
    awaitRelease: Boolean = true,
) {
    // The menu opens while the OK button is still physically HELD (long-press).
    // Ignore every OK event — held repeats AND the final release — until the
    // button has been fully released once. Only a fresh press activates items.
    var released by remember { mutableStateOf(!awaitRelease) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.onPreviewKeyEvent { e ->
                val select = e.key == Key.DirectionCenter || e.key == Key.Enter ||
                    e.key == Key.NumPadEnter
                when {
                    !select -> false
                    released -> false // normal operation after the first release
                    e.type == KeyEventType.KeyUp -> { released = true; true }
                    else -> true // swallow auto-repeats of the initiating hold
                }
            },
        ) {
            Column(Modifier.width(320.dp).padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
                val firstFocus = remember { FocusRequester() }
                entries.forEachIndexed { index, entry ->
                    ListItem(
                        selected = false,
                        onClick = entry.action,
                        headlineContent = { Text(entry.label) },
                        leadingContent = {
                            Icon(entry.icon, contentDescription = null)
                        },
                        modifier = if (index == 0) Modifier.focusRequester(firstFocus) else Modifier,
                    )
                }
                LaunchedEffect(Unit) {
                    runCatching { firstFocus.requestFocus() }
                }
            }
        }
    }
}
