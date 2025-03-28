package ink.magma.zthPreferences;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import java.util.Optional;

public enum PreferenceType {
    DROP_ITEMS("drop_items", "物品丢弃", NamedTextColor.GRAY, true),
    TRAMPLE_CROPS("trample_crops", "耕地踩踏", NamedTextColor.GRAY, false),
    PICKUP_ITEMS("pickup_items", "物品拾取", NamedTextColor.GRAY, true);

    private final String key;
    private final String displayName;
    private final TextColor color;
    private final boolean defaultValue;

    PreferenceType(String key, String displayName, TextColor color, boolean defaultValue) {
        this.key = key;
        this.displayName = displayName;
        this.color = color;
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TextColor getColor() {
        return color;
    }

    public static Optional<PreferenceType> fromKey(String key) {
        for (PreferenceType type : values()) {
            if (type.key.equals(key)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}