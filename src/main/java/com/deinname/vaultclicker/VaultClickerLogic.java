package com.deinname.vaultclicker;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

public class VaultClickerLogic {
    // Variablen für den Anti-Cheat geschützten Delay & Spam-Schutz
    private static long clickTime = 0;
    private static boolean isWaitingForClick = false;
    private static boolean alreadyClickedThisCore = false;
    private static BlockHitResult targetHit = null;

    public static void tick(MinecraftClient client) {
        // Grundvoraussetzungen prüfen
        if (!VaultClickerMod.isEnabled || client.player == null || client.world == null) {
            resetState();
            return;
        }

        // 1. Hat der Spieler den Ominous Trial Key in der Hand?
        boolean hasOminousKeyMain = client.player.getMainHandStack().isOf(Items.OMINOUS_TRIAL_KEY);
        boolean hasOminousKeyOff = client.player.getOffHandStack().isOf(Items.OMINOUS_TRIAL_KEY);
        
        if (!hasOminousKeyMain && !hasOminousKeyOff) {
            resetState();
            return;
        }

        // 2. Worauf schaut der Spieler gerade?
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
            BlockPos pos = hit.getBlockPos();
            BlockEntity blockEntity = client.world.getBlockEntity(pos);

            // 3. Ist es ein Vault?
            if (blockEntity instanceof VaultBlockEntity vault) {
                // Das aktuell im Vault rotierende Item auslesen
                ItemStack displayItem = vault.getSharedData().getDisplayItem();

                // 4. Ist das rotierende Item ein Heavy Core?
                if (displayItem.isOf(Items.HEAVY_CORE)) {
                    
                    // Verhindert Hunderte Klicks, wenn der Core länger angezeigt wird
                    if (!alreadyClickedThisCore) {
                        
                        if (!isWaitingForClick) {
                            // HUMANIZED DELAY INITIALISIEREN
                            // Generiert eine zufällige Verzögerung zwischen 80ms und 150ms
                            long randomDelay = ThreadLocalRandom.current().nextLong(80, 151);
                            clickTime = System.currentTimeMillis() + randomDelay;
                            isWaitingForClick = true;
                            targetHit = hit;
                        } 
                        else if (System.currentTimeMillis() >= clickTime) {
                            // DELAY IST ABGELAUFEN -> KLICK AUSFÜHREN
                            Hand handToUse = hasOminousKeyMain ? Hand.MAIN_HAND : Hand.OFF_HAND;

                            // Sichere / legitime Methode, um mit dem Block zu interagieren (wie Vanilla-Minecraft)
                            client.interactionManager.interactBlock(client.player, handToUse, targetHit);
                            client.player.swingHand(handToUse);

                            // Spam-Schutz aktivieren
                            alreadyClickedThisCore = true;
                            isWaitingForClick = false;
                        }
                    }
                } else {
                    // Item ist kein Heavy Core (oder nicht mehr) -> Spam-Schutz zurücksetzen
                    alreadyClickedThisCore = false;
                    isWaitingForClick = false;
                }
            } else {
                // Spieler schaut auf einen Block, aber es ist kein Vault
                resetState();
            }
        } else {
            // Spieler schaut ins Leere oder auf ein Entity
            resetState();
        }
    }

    // Setzt die internen States zurück, wenn der Spieler wegschaut oder das Item weg ist
    private static void resetState() {
        isWaitingForClick = false;
        alreadyClickedThisCore = false;
        targetHit = null;
    }
}
