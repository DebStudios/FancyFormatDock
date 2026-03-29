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
                .setTitle(Component.literal("Format Panel Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        // enabled toggle
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Enabled"), FormatPanelConfig.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> FormatPanelConfig.enabled = val)
                .build());

        // format mode dropdown
        general.addEntry(entryBuilder.startEnumSelector(
                        Component.literal("Format Mode"),
                        FormatPanelConfig.FormatMode.class,
                        FormatPanelConfig.formatMode)
                .setDefaultValue(FormatPanelConfig.FormatMode.ESSENTIALS)
                .setSaveConsumer(val -> FormatPanelConfig.formatMode = val)
                .build());

        builder.setSavingRunnable(FormatPanelConfig::save);

        return builder.build();
    }
}