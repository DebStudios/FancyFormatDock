package net.debrooo.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class FormatPanelConfig {

    public boolean enabled = true;
    public FormatMode formatMode = FormatMode.EssentialsX;

    public static boolean enabledStatic = true;
    public static FormatMode formatModeStatic = FormatMode.EssentialsX;

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("fancyformatdock.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum FormatMode {
        Vanilla,
        EssentialsX,
        MiniMessage
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
            FormatPanelConfig data = GSON.fromJson(r, FormatPanelConfig.class);
            enabledStatic = data.enabled;
            formatModeStatic = data.formatMode;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            FormatPanelConfig data = new FormatPanelConfig();
            data.enabled = enabledStatic;
            data.formatMode = formatModeStatic;
            GSON.toJson(data, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}