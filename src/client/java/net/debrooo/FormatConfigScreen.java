package net.debrooo.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FormatConfigScreen implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent);
    }

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("FancyFormatDock Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ── General ──────────────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Enabled"), FormatPanelConfig.enabledStatic)
                .setDefaultValue(true)
                .setSaveConsumer(val -> FormatPanelConfig.enabledStatic = val)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(
                        Component.literal("Format Mode"),
                        FormatPanelConfig.FormatMode.class,
                        FormatPanelConfig.formatModeStatic)
                .setDefaultValue(FormatPanelConfig.FormatMode.EssentialsX)
                .setSaveConsumer(val -> FormatPanelConfig.formatModeStatic = val)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(
                        Component.literal("Color Picker Quality"),
                        FormatPanelConfig.RenderQuality.class,
                        FormatPanelConfig.colorPickerQuality)
                .setDefaultValue(FormatPanelConfig.RenderQuality.Medium)
                .setSaveConsumer(val -> FormatPanelConfig.colorPickerQuality = val)
                .build());

        // ── Preset Colors ─────────────────────────────────────────────────────
        ConfigCategory presets = builder.getOrCreateCategory(Component.literal("Preset Colors"));

        presets.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Preset Color Buttons"), FormatPanelConfig.presetButtonsEnabled)
                .setDefaultValue(false)
                .setSaveConsumer(val -> FormatPanelConfig.presetButtonsEnabled = val)
                .build());

        String[] presetLabels = {"Button 1", "Button 2", "Button 3", "Button 4"};

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            int currentColor = (int) Long.parseLong("FF" + FormatPanelConfig.presetButtons[idx].hex, 16) & 0x00FFFFFF;

            presets.addEntry(entryBuilder.startSubCategory(
                            Component.literal(presetLabels[i]),
                            List.of(
                                    entryBuilder.startColorField(
                                                    Component.literal("Color"), currentColor)
                                            .setDefaultValue(0xFF0000)
                                            .setSaveConsumer(val -> FormatPanelConfig.presetButtons[idx].hex = String.format("%06X", val & 0x00FFFFFF))
                                            .build(),
                                    entryBuilder.startBooleanToggle(
                                                    Component.literal("Enabled"), FormatPanelConfig.presetButtons[idx].enabled)
                                            .setDefaultValue(true)
                                            .setSaveConsumer(val -> FormatPanelConfig.presetButtons[idx].enabled = val)
                                            .build()
                            ))
                    .build());
        }

        builder.setSavingRunnable(FormatPanelConfig::save);

        return builder.build();
    }
}