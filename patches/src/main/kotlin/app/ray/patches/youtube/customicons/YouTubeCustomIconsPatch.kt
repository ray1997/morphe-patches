package app.ray.patches.youtube.customicons

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.ray.patches.shared.Constants.COMPATIBILITY_YOUTUBE

private const val DRAWABLE_DIR = "res/drawable/"

/**
 * YouTube stores some icons as vectors and some as grayscale PNGs in the density folders.
 * A vector is rewritten in place. For a PNG-only icon the density bitmaps are deleted and a
 * vector with the same resource name is added to `res/drawable/`, so every reference keeps
 * working.
 */
private val RASTER_DIRS = listOf(
    "drawable",
    "drawable-mdpi",
    "drawable-hdpi",
    "drawable-xhdpi",
    "drawable-xxhdpi",
    "drawable-xxxhdpi",
    "drawable-nodpi",
    "drawable-anydpi",
    "drawable-anydpi-v21",
    "drawable-anydpi-v24",
    "drawable-anydpi-v26",
    "drawable-anydpi-v31",
    "drawable-anydpi-v33",
    "drawable-anydpi-v35",
)

private val PATH_TAG = Regex("""<path\b[^>]*>""")
private val PATH_DATA = Regex("""android:pathData="([^"]+)"""")
private val WIDTH = Regex("""android:width="([^"]+)"""")
private val HEIGHT = Regex("""android:height="([^"]+)"""")
private val VIEWPORT_WIDTH = Regex("""android:viewportWidth="([^"]+)"""")
private val VIEWPORT_HEIGHT = Regex("""android:viewportHeight="([^"]+)"""")
private val ROOT_TINT = Regex("""android:tint="([^"]*)"""")
private val ROOT_AUTO_MIRRORED = Regex("""android:autoMirrored="([^"]*)"""")
private val ROOT_ALPHA = Regex("""android:alpha="([^"]*)"""")

private object YouTubeFluentIcons

private data class Placement(val scale: Float, val dx: Float, val dy: Float)

private fun fmt(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

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

private fun colors(tag: String): List<String> {
    val fillAlpha = attr(tag, "fillAlpha")?.toFloatOrNull() ?: 1f
    val strokeAlpha = attr(tag, "strokeAlpha")?.toFloatOrNull() ?: 1f
    return listOfNotNull(
        attr(tag, "fillColor")?.takeIf { fillAlpha > 0.05f },
        attr(tag, "strokeColor")?.takeIf { strokeAlpha > 0.05f },
    )
}

private fun placement(viewportWidth: Float, viewportHeight: Float): Placement {
    val scale = minOf(viewportWidth, viewportHeight) / 24f
    return Placement(scale, (viewportWidth - 24f * scale) / 2f, (viewportHeight - 24f * scale) / 2f)
}

/** The Fluent path, or null when the vector is a selector, an animation or a composed badge. */
private fun replaceVector(original: String, fluent: String): String? {
    if ("<vector" !in original) return null
    val width = WIDTH.find(original)?.groupValues?.get(1) ?: return null
    val height = HEIGHT.find(original)?.groupValues?.get(1) ?: return null
    val vw = VIEWPORT_WIDTH.find(original)?.groupValues?.get(1)?.toFloatOrNull() ?: return null
    val vh = VIEWPORT_HEIGHT.find(original)?.groupValues?.get(1)?.toFloatOrNull() ?: return null
    val paths = PATH_TAG.findAll(original).map { it.value }.toList()
    val visible = paths.asSequence().flatMap { colors(it).asSequence() }
        .filterNot { isTransparent(it) }.toList()
    if (visible.isEmpty()) return null
    val tint = ROOT_TINT.find(original)?.groupValues?.get(1)
    // A tinted vector supplies the colour at runtime. An untinted one must have one colour.
    if (tint == null && visible.distinct().size > 1) return null
    val color = visible.first()

    val box = placement(vw, vh)
    return buildString {
        appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        appendLine("""<vector xmlns:android="http://schemas.android.com/apk/res/android"""")
        appendLine("""    android:width="$width" android:height="$height"""")
        appendLine("""    android:viewportWidth="$vw" android:viewportHeight="$vh"""")
        tint?.let { appendLine("""    android:tint="$it"""") }
        ROOT_AUTO_MIRRORED.find(original)?.groupValues?.get(1)?.let {
            appendLine("""    android:autoMirrored="$it"""")
        }
        ROOT_ALPHA.find(original)?.groupValues?.get(1)?.let {
            appendLine("""    android:alpha="$it"""")
        }
        appendLine(">")
        appendLine(
            """    <group android:translateX="${fmt(box.dx)}" android:translateY="${fmt(box.dy)}" """ +
                """android:scaleX="${fmt(box.scale)}" android:scaleY="${fmt(box.scale)}">""",
        )
        appendLine("""        <path android:fillColor="$color" android:pathData="$fluent" />""")
        appendLine("    </group>")
        appendLine("</vector>")
    }
}

private fun rasterVector(sizeDp: Int, color: String, fluent: String): String = buildString {
    appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
    appendLine("""<vector xmlns:android="http://schemas.android.com/apk/res/android"""")
    appendLine("""    android:width="${sizeDp}dp" android:height="${sizeDp}dp"""")
    appendLine("""    android:viewportWidth="24" android:viewportHeight="24">""")
    appendLine("""    <path android:fillColor="$color" android:pathData="$fluent" />""")
    appendLine("</vector>")
}

private fun fluentPath(name: String): String {
    val stream = YouTubeFluentIcons::class.java.getResourceAsStream("/fluent-icons/$name.xml")
        ?: throw PatchException("Missing Fluent icon $name in the patch bundle")
    val text = stream.bufferedReader().use { it.readText() }
    return PATH_DATA.find(text)?.groupValues?.get(1)
        ?: throw PatchException("Fluent icon $name has no path")
}

@Suppress("unused")
val youtubeCustomIconsPatch = resourcePatch(
    name = "Custom icons mod (YouTube)",
    description = "Replaces YouTube's bottom bar, top bar, player controls and common action icons " +
        "with Microsoft Fluent icons. PNG-only icons are rebuilt as vectors with the same resource " +
        "name. Brand, illustration and thumbnail assets stay unchanged.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    execute {
        var replaced = 0
        for (target in YOUTUBE_ICON_TARGETS) {
            val path = fluentPath(target.fluent)
            if (target.mode == IconMode.VECTOR) {
                val file = get("$DRAWABLE_DIR${target.drawable}.xml")
                if (!file.exists()) continue
                val updated = replaceVector(file.readText(), path) ?: continue
                file.writeText(updated)
                replaced++
                continue
            }

            var deleted = false
            for (dir in RASTER_DIRS) {
                for (extension in listOf("png", "webp")) {
                    val resource = "res/$dir/${target.drawable}.$extension"
                    if (!get(resource).exists()) continue
                    delete(resource)
                    deleted = true
                }
            }
            if (!deleted) continue
            get("$DRAWABLE_DIR${target.drawable}.xml").writeText(
                rasterVector(target.sizeDp, target.color, path),
            )
            replaced++
        }
        if (replaced < 100) {
            throw PatchException(
                "Custom icons mod replaced only $replaced YouTube drawables. The icon names may have changed.",
            )
        }
    }
}
