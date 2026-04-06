package net.debrooo.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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

        builder.setSavingRunnable(FormatPanelConfig::save);

        general.addEntry(entryBuilder.startEnumSelector(
                        Component.literal("Color Picker Quality"),
                        FormatPanelConfig.RenderQuality.class,
                        FormatPanelConfig.colorPickerQuality)
                .setDefaultValue(FormatPanelConfig.RenderQuality.Medium)
                .setSaveConsumer(val -> FormatPanelConfig.colorPickerQuality = val)
                .build());

        return builder.build();


    }


}