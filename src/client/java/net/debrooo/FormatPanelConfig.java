package net.debrooo.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class FormatPanelConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("format_panel.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Config values
    public int formatMode = 1;   // 0=Vanilla, 1=Essentials, 2=MiniMessage
    public boolean enabled = true;

    private static FormatPanelConfig instance;

    public static FormatPanelConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(r, FormatPanelConfig.class);
            } catch (Exception e) {
                instance = new FormatPanelConfig();
            }
        } else {
            instance = new FormatPanelConfig();
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}