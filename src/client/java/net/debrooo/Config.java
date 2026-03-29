package net.debrooo.client;

import eu.midnightdust.lib.config.MidnightConfig;

public class FormatPanelConfig extends MidnightConfig {

    @Comment(centered = true)
    public static MidnightConfig.Comment generalComment;

    @Entry
    public static boolean enabled = true;

    @Comment(centered = true)
    public static MidnightConfig.Comment modeComment;

    @Entry
    public static FormatMode formatMode = FormatMode.ESSENTIALS;

    public enum FormatMode {
        VANILLA,
        ESSENTIALS,
        MINIMESSAGE
    }
}