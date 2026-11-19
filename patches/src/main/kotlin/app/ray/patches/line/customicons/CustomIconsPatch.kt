package app.ray.patches.line.customicons

import app.ray.patches.shared.Constants.COMPATIBILITY_LINE
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch

private const val DRAWABLE_DIR = "res/drawable/"

private val PATH_TAG = Regex("""<path\b[^>]*>""")
private val PATH_DATA = Regex("""android:pathData="([^"]+)"""")
private val WIDTH = Regex("""android:width="([^"]+)"""")
private val HEIGHT = Regex("""android:height="([^"]+)"""")
private val VIEWPORT_WIDTH = Regex("""android:viewportWidth="([^"]+)"""")
private val VIEWPORT_HEIGHT = Regex("""android:viewportHeight="([^"]+)"""")
private val BADGE_COLORS = setOf("#ff334b", "#ffff334b")

/**
 * LINE draws its UI icons as small vector drawables. Each one is replaced here with the path of a
 * 24dp Microsoft Fluent icon, scaled into the original glyph box, so the layout, the touch target
 * and the theme colour stay as LINE made them. Extra red "new" dots stay in place.
 *
 * The replacement keeps LINE's colour attribute, including `@color/...` references, so light and
 * dark themes keep working. Brand marks (Apple, Google, PayPay, LINE Premium) are not in the table.
 */
private object FluentIcons

/** The Fluent path and the box that it is drawn in, for one target. */
private data class Placement(val scale: Float, val dx: Float, val dy: Float)

private fun fluentPath(name: String): String {
    val stream = FluentIcons::class.java.getResourceAsStream("/fluent-icons/$name.xml")
        ?: throw PatchException("Missing Fluent icon $name in the patch bundle")
    val text = stream.bufferedReader().use { it.readText() }
    return PATH_DATA.find(text)?.groupValues?.get(1)
        ?: throw PatchException("Fluent icon $name has no path")
}

private fun attr(tag: String, name: String): String? =
    Regex("""android:$name="([^"]*)"""").find(tag)?.groupValues?.get(1)

private fun isTransparent(color: String?): Boolean {
    if (color == null) return true
    if (color == "@color/transparent" || color == "@android:color/transparent") return true
    if (!color.startsWith("#")) return false
    val hex = color.substring(1)
    return when (hex.length) {
        8 -> hex.startsWith("00")
        4 -> hex.startsWith("0")
        else -> false
    }
}

/** The visible colours of a path. A nearly transparent hit-area path has no visible colour. */
private fun colors(tag: String): List<String> {
    val fillAlpha = attr(tag, "fillAlpha")?.toFloatOrNull() ?: 1f
    val strokeAlpha = attr(tag, "strokeAlpha")?.toFloatOrNull() ?: 1f
    return listOfNotNull(
        attr(tag, "fillColor")?.takeIf { fillAlpha > 0.05f },
        attr(tag, "strokeColor")?.takeIf { strokeAlpha > 0.05f },
    )
}

/** Small viewports are drawn in their own size. The big ones get a glyph box inside the padding. */
private fun placement(name: String, viewportWidth: Float, viewportHeight: Float): Placement = when {
    name.startsWith("navi_bottom_") || name.startsWith("timeline_navi_bottom_") ->
        Placement(20f / 24f, 22f, 40f)
    viewportWidth == 38f && viewportHeight == 38f -> Placement(20f / 24f, 9f, 9f)
    viewportWidth == 36f && viewportHeight == 36f -> Placement(28f / 24f, 4f, 4f)
    viewportWidth == 28f && viewportHeight == 28f -> Placement(26f / 24f, 1f, 1f)
    viewportWidth == 22f && viewportHeight == 22f -> Placement(21f / 24f, 0.5f, 0.5f)
    viewportWidth == 67f && viewportHeight == 67f -> Placement(40f / 24f, 13.5f, 13.5f)
    viewportWidth == 69f && viewportHeight == 69f -> Placement(39f / 24f, 15f, 15f)
    else -> {
        val scale = minOf(viewportWidth, viewportHeight) / 24f
        Placement(scale, (viewportWidth - 24f * scale) / 2f, (viewportHeight - 24f * scale) / 2f)
    }
}

private fun fmt(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

/**
 * The new drawable, or null when the file has no visible icon colour (a selector, a mask or a
 * gradient) and must stay as it is.
 */
private fun replaceIcon(name: String, original: String, fluent: String): String? {
    val width = WIDTH.find(original)?.groupValues?.get(1) ?: return null
    val height = HEIGHT.find(original)?.groupValues?.get(1) ?: return null
    val viewportWidth = VIEWPORT_WIDTH.find(original)?.groupValues?.get(1)?.toFloatOrNull() ?: return null
    val viewportHeight = VIEWPORT_HEIGHT.find(original)?.groupValues?.get(1)?.toFloatOrNull() ?: return null

    val paths = PATH_TAG.findAll(original).map { it.value }.toList()
    val iconColor = paths.asSequence()
        .flatMap { colors(it).asSequence() }
        .firstOrNull { !isTransparent(it) && it.lowercase() !in BADGE_COLORS }
        ?: return null

    // The red "new" dot sits outside the glyph box, so it is copied unchanged.
    val extras = paths.filter { tag ->
        colors(tag).any { it.lowercase() in BADGE_COLORS }
    }

    val box = placement(name, viewportWidth, viewportHeight)
    return buildString {
        appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        appendLine("""<vector xmlns:android="http://schemas.android.com/apk/res/android"""")
        appendLine("""    android:width="$width" android:height="$height"""")
        appendLine("""    android:viewportWidth="$viewportWidth" android:viewportHeight="$viewportHeight">""")
        appendLine(
            """    <group android:translateX="${fmt(box.dx)}" android:translateY="${fmt(box.dy)}" """ +
                """android:scaleX="${fmt(box.scale)}" android:scaleY="${fmt(box.scale)}">""",
        )
        appendLine("""        <path android:fillColor="$iconColor" android:pathData="$fluent" />""")
        appendLine("    </group>")
        for (extra in extras) appendLine("    $extra")
        appendLine("</vector>")
    }
}

@Suppress("unused")
val customIconsPatch = resourcePatch(
    name = "Custom icons mod",
    description = "Replaces 203 LINE drawables (tab bar, top bars, Chats and Friends headers, " +
        "Settings rows and Home) with Microsoft Fluent icons. Colours follow LINE's light and " +
        "dark themes. Account and brand logos stay unchanged.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        var replaced = 0
        for (target in ICON_TARGETS) {
            val file = get("$DRAWABLE_DIR${target.drawable}.xml")
            if (!file.exists()) continue
            val original = file.readText()
            if ("<path" !in original) continue
            val updated = replaceIcon(target.drawable, original, fluentPath(target.fluent)) ?: continue
            file.writeText(updated)
            replaced++
        }
        if (replaced < 100) {
            throw PatchException(
                "Custom icons mod replaced only $replaced drawables. LINE's icon names may have changed.",
            )
        }
    }
}
