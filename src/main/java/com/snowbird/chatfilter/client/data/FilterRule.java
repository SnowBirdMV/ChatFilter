package com.snowbird.chatfilter.client.data;

public class FilterRule {
    private String name;
    private boolean isRegex;
    private boolean isOverlay;
    private String filterText;

    public FilterRule(String name, String filterText, boolean isRegex, boolean isOverlay) {
        this.name = name;
        this.isRegex = isRegex;
        this.isOverlay = isOverlay;
        this.filterText = filterText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRegex() {
        return isRegex;
    }

    public void setRegex(boolean regex) {
        isRegex = regex;
    }

    public boolean isOverlay() {
        return isOverlay;
    }

    public void setOverlay(boolean overlay) {
        isOverlay = overlay;
    }

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }
}
