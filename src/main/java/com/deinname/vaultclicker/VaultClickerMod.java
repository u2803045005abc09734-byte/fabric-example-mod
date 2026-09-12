package com.deinname.vaultclicker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class VaultClickerMod implements ClientModInitializer {
    public static boolean isEnabled = false;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        // Keybind registrieren (Standard: V)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vaultclicker.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.vaultclicker.general"
        ));

        // Client Tick Event abonnieren
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Prüfen, ob der Key gedrückt wurde
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                if (client.player != null) {
                    if (isEnabled) {
                        client.player.sendMessage(Text.literal("§aVault-Clicker: ON"), false);
                    } else {
                        client.player.sendMessage(Text.literal("§cVault-Clicker: OFF"), false);
                    }
                }
            }

            // Die Logik in jedem Tick aufrufen
            VaultClickerLogic.tick(client);
        });
    }
}
