package net.debrooo.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

public class FormatConfigCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommandManager.literal("formatconfig")
                                .executes(ctx -> {
                                    Minecraft.getInstance().execute(() ->
                                            Minecraft.getInstance().setScreen(new FormatConfigScreen())
                                    );
                                    return 1;
                                })
                )
        );
    }
}