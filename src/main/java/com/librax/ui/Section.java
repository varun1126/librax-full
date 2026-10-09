package com.librax.ui;

/**
 * Application sections reachable from the sidebar navigation.
 *
 * <p>Each section carries its navigation identity (title, subtitle, icon),
 * the sidebar group it belongs to, and whether its content scrolls inside
 * the workspace (list pages scroll internally instead).</p>
 */
public enum Section {

    DASHBOARD("Dashboard", "Library overview and circulation activity", "dashboard", true),
    BOOKS("Books", "Catalog, copies and inventory", "book", false),
    MEMBERS("Members", "Patrons and memberships", "users", false),
    LOANS("Loans", "Issue and return desk", "loans", false),
    RESERVATIONS("Reservations", "Hold queue and pickups", "reservations", false),
    FINES("Fines", "Penalties, waivers and payments", "fines", false),
    REPORTS("Reports", "Exports and summaries", "reports", true),
    SETTINGS("Settings", "Application preferences", "settings", true);

    /** Sidebar groups in display order. */
    public static final String[] GROUPS = {"OVERVIEW", "CATALOG", "CIRCULATION", "SYSTEM"};

    /** Navigation label shown in the sidebar and top bar. */
    public final String title;
    /** Supporting line shown next to the title in the top bar. */
    public final String subtitle;
    /** Icon file name (without extension) used in the sidebar. */
    public final String iconName;
    /** Whether the section's content is wrapped in a scroll pane. */
    public final boolean scrollable;

    Section(String title, String subtitle, String iconName, boolean scrollable) {
        this.title = title;
        this.subtitle = subtitle;
        this.iconName = iconName;
        this.scrollable = scrollable;
    }

    /**
     * Sidebar group this section is listed under.
     *
     * @return one of {@link #GROUPS}
     */
    public String group() {
        return switch (this) {
            case DASHBOARD -> "OVERVIEW";
            case BOOKS, MEMBERS -> "CATALOG";
            case LOANS, RESERVATIONS, FINES -> "CIRCULATION";
            case REPORTS, SETTINGS -> "SYSTEM";
        };
    }

    /**
     * Sections belonging to a group, in display order.
     *
     * @param group group name as returned by {@link #group()}
     * @return matching sections
     */
    public static Section[] inGroup(String group) {
        java.util.List<Section> result = new java.util.ArrayList<>();
        for (Section section : values()) {
            if (section.group().equals(group)) {
                result.add(section);
            }
        }
        return result.toArray(new Section[0]);
    }
}
