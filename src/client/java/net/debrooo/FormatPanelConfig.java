package net.debrooo.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class FormatPanelConfig {

    public boolean enabledField = true;
    public FormatMode formatModeField = FormatMode.EssentialsX;
    public RenderQuality colorPickerQualityField = RenderQuality.Medium;
    public boolean presetButtonsEnabledField = false;
    public boolean showFormatWarningField = false;
    public boolean showFormatCodeLabelsField = true;
    public PresetButton[] presetButtonsField = {
            new PresetButton(), new PresetButton(),
            new PresetButton(), new PresetButton()
    };

    public static boolean enabledStatic = true;
    public static FormatMode formatModeStatic = FormatMode.EssentialsX;
    public static RenderQuality colorPickerQuality = RenderQuality.Medium;
    public static boolean presetButtonsEnabled = false;
    public static boolean showFormatWarning = false;
    public static boolean showFormatCodeLabels = true;
    public static PresetButton[] presetButtons = {
            new PresetButton(), new PresetButton(),
            new PresetButton(), new PresetButton()
    };

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("fancyformatdock.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum FormatMode {
        Vanilla,
        EssentialsX,
        MiniMessage
    }

    public enum RenderQuality {
        Low,
        Medium,
        High
    }

    public static class PresetButton {
        public String hex = "FF0000";
        public boolean enabled = true;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
            FormatPanelConfig data = GSON.fromJson(r, FormatPanelConfig.class);
            enabledStatic        = data.enabledField;
            formatModeStatic     = data.formatModeField != null         ? data.formatModeField         : FormatMode.EssentialsX;
            colorPickerQuality   = data.colorPickerQualityField != null ? data.colorPickerQualityField : RenderQuality.Medium;
            presetButtonsEnabled = data.presetButtonsEnabledField;
            showFormatWarning    = data.showFormatWarningField;
            showFormatCodeLabels = data.showFormatCodeLabelsField;
            if (data.presetButtonsField != null && data.presetButtonsField.length == 4) {
                presetButtons = data.presetButtonsField;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            FormatPanelConfig data = new FormatPanelConfig();
            data.enabledField              = enabledStatic;
            data.formatModeField           = formatModeStatic;
            data.colorPickerQualityField   = colorPickerQuality;
            data.presetButtonsEnabledField = presetButtonsEnabled;
            data.showFormatWarningField    = showFormatWarning;
            data.showFormatCodeLabelsField = showFormatCodeLabels;
            data.presetButtonsField        = presetButtons;
            GSON.toJson(data, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}