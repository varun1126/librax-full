package com.librax.ui.theme;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Access point for the application icon set ({@code resources/icons/*.svg}).
 *
 * <p>Icons are monochrome line icons; a {@link FlatSVGIcon.ColorFilter}
 * tints them to any semantic color at runtime, so the same artwork works on
 * the dark sidebar, on light surfaces and inside accent-filled buttons.
 * Derived icons are cached per (name, size, color).</p>
 */
public final class UiIcons {

    private static final Logger log = LoggerFactory.getLogger(UiIcons.class);

    /** Default toolbar / button icon size. */
    public static final int SMALL = 14;
    /** Sidebar navigation icon size. */
    public static final int NAV = 16;
    /** Empty states and section glyphs. */
    public static final int LARGE = 22;

    private static final Map<String, FlatSVGIcon> CACHE = new ConcurrentHashMap<>();

    private UiIcons() {
    }

    /**
     * Returns a tinted icon at the default size.
     *
     * @param name  icon file name without the {@code .svg} extension
     * @param color tint applied to the artwork
     * @return a cached, reusable icon instance
     */
    public static Icon of(String name, Color color) {
        return of(name, color, SMALL);
    }

    /**
     * Returns a tinted icon at a specific size.
     *
     * @param name  icon file name without the {@code .svg} extension
     * @param color tint applied to the artwork
     * @param size  rendered size in pixels (square)
     * @return a cached, reusable icon instance
     */
    public static Icon of(String name, Color color, int size) {
        String key = name + '@' + size + '@' + (color == null ? "auto" : Integer.toString(color.getRGB(), 16));
        return CACHE.computeIfAbsent(key, k -> create(name, color, size));
    }

    private static FlatSVGIcon create(String name, Color color, int size) {
        FlatSVGIcon icon = new FlatSVGIcon("icons/" + name + ".svg", size, size);
        if (color != null) {
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
        }
        if (!icon.hasFound()) {
            log.warn("Icon resource not found: icons/{}.svg", name);
        }
        return icon;
    }
}
