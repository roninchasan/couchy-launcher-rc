package com.conreo.couchytv.data

import android.content.Context
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import androidx.compose.ui.graphics.Color
import com.conreo.couchytv.ui.AppIcons

/** TV input shortcuts (HDMI, AV...) — treated like apps: movable, hideable, sectioned. */
object Inputs {

    const val PREFIX = "input:"

    fun isInput(pkg: String) = pkg.startsWith(PREFIX)

    fun idOf(pkg: String) = pkg.removePrefix(PREFIX)

    fun load(context: Context): List<AppEntry> {
        val tvm = context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
            ?: return emptyList()
        val all = runCatching { tvm.tvInputList }.getOrNull().orEmpty()
            .filter { it.isPassthroughInput }
        // A CEC device (e.g. a console) is listed as a child of its HDMI port; show
        // the named child instead of the generic port.
        val parents = all.mapNotNull { it.parentId }.toSet()
        return all
            .filter { it.id !in parents }
            .sortedBy { it.id }
            .map { entry(context, it) }
    }

    const val ICON_HDMI = "hdmi"
    const val ICON_XBOX = "xbox"
    const val ICON_GAMEPAD = "gamepad"
    private val ICON_CYCLE = listOf(ICON_HDMI, ICON_XBOX, ICON_GAMEPAD)

    /** Icon choice currently shown for [app]: the user's pick, else guessed from its name. */
    fun iconOf(app: AppEntry, picked: String?): String = picked ?: when {
        app.label.contains("xbox", ignoreCase = true) -> ICON_XBOX
        app.label.contains("playstation", ignoreCase = true) ||
            app.label.contains("switch", ignoreCase = true) -> ICON_GAMEPAD
        else -> ICON_HDMI
    }

    fun nextIcon(app: AppEntry, picked: String?): String =
        ICON_CYCLE[(ICON_CYCLE.indexOf(iconOf(app, picked)) + 1) % ICON_CYCLE.size]

    /** Applies the icon choice (glyph and tile color) to a loaded input. */
    fun styled(app: AppEntry, picked: String?): AppEntry {
        val icon = iconOf(app, picked)
        return app.copy(
            glyph = when (icon) {
                ICON_XBOX -> AppIcons.Xbox
                ICON_GAMEPAD -> AppIcons.Gamepad
                else -> AppIcons.Hdmi
            },
            tile = if (icon == ICON_XBOX) Color(0xFF107C10) else Color(0xFF263238),
        )
    }

    private fun entry(context: Context, info: TvInputInfo): AppEntry {
        val label = (info.loadCustomLabel(context) ?: info.loadLabel(context))?.toString()
            ?: info.id
        return AppEntry(
            pkg = PREFIX + info.id,
            label = label,
            banner = null,
            icon = null,
            autoCategory = "inputs",
            tile = Color(0xFF263238),
            stamp = 0L,
            firstInstall = 0L,
            glyph = AppIcons.Hdmi,
        )
    }
}
