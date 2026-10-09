package com.librax.ui.components;

import com.librax.ui.theme.Theme;

import java.awt.Color;

/**
 * Semantic tones used by status badges and inline status text.
 *
 * <p>Each tone carries a quiet tinted background plus a readable foreground
 * derived from the same palette, which keeps badges legible on both the
 * light and dark themes and on striped table rows.</p>
 */
public enum BadgeTone {

    /** Positive / completed states: available, paid, returned, fulfilled. */
    SUCCESS("LibraX.success.subtle", "LibraX.success.text", "LibraX.success"),
    /** Attention states: waiting, due soon, partially paid, expiring. */
    WARNING("LibraX.warning.subtle", "LibraX.warning.text", "LibraX.warning"),
    /** Problem states: overdue, unpaid, suspended, cancelled, lost. */
    DANGER("LibraX.danger.subtle", "LibraX.danger.text", "LibraX.danger"),
    /** In-progress states: on loan, issued, ready for pickup. */
    INFO("LibraX.info.subtle", "LibraX.info.text", "LibraX.info"),
    /** States without a valence: unknown, not applicable, archived. */
    NEUTRAL("LibraX.neutral.subtle", "LibraX.neutral.text", "LibraX.text.muted");

    private final String backgroundKey;
    private final String foregroundKey;
    private final String strongKey;

    BadgeTone(String backgroundKey, String foregroundKey, String strongKey) {
        this.backgroundKey = backgroundKey;
        this.foregroundKey = foregroundKey;
        this.strongKey = strongKey;
    }

    /** Tinted badge background. */
    public Color background() {
        return Theme.color(backgroundKey);
    }

    /** Badge label color; readable on {@link #background()}. */
    public Color foreground() {
        return Theme.color(foregroundKey);
    }

    /** Saturated tone color used for dots, bars and inline emphasis. */
    public Color strong() {
        return Theme.color(strongKey);
    }

    /**
     * Best-effort tone for a raw status value.
     *
     * <p>Callers that render statuses from different domains (for example
     * {@code ACTIVE} meaning an active loan versus an active membership)
     * should pass an explicit tone instead of relying on this mapping.</p>
     *
     * @param value raw enum name or display text, may be {@code null}
     * @return the matching tone, or {@link #NEUTRAL} when unknown
     */
    public static BadgeTone of(Object value) {
        if (value == null) {
            return NEUTRAL;
        }
        String text = Theme.normalize(String.valueOf(value)).replace('-', '_').replace(' ', '_');
        return switch (text) {
            case "AVAILABLE", "PAID", "FULFILLED", "RETURNED", "WAIVED", "ACTIVE", "COMPLETED", "OK" -> SUCCESS;
            case "WAITING", "DUE_SOON", "PENDING", "PARTIALLY_PAID", "EXPIRED", "LOW_STOCK", "ON_HOLD" -> WARNING;
            case "OVERDUE", "UNPAID", "SUSPENDED", "OUTSTANDING", "CANCELLED", "LOST", "DAMAGED", "RETIRED" -> DANGER;
            case "ISSUED", "READY", "IN_PROGRESS" -> INFO;
            default -> NEUTRAL;
        };
    }
}
