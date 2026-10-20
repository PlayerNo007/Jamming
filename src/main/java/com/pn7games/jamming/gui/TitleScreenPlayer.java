package com.pn7games.jamming.gui;

import com.pn7games.jamming.client.JammingKeys;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

// A small music player in the top-left corner of the main menu:
//   [prev][play][next][loop][♫]  Now playing title
//                                 artist · dimension
// [♫] (or the "Open Jamming" key) opens the full Jamming menu.
public final class TitleScreenPlayer {

    private static final int X = 4;
    private static final int Y = 4;

    private TitleScreenPlayer() {
    }

    public static void register() {
        // Runs every time any screen is (re)built. We only touch the title screen.
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof TitleScreen)) {
                return;
            }
            // Screens.getWidgets() is Fabric's way to add buttons to a vanilla screen.
            Screens.getWidgets(screen).addAll(MusicControls.create(client, X, Y));
            Screens.getWidgets(screen).add(new IconButton(X + MusicControls.WIDTH + MusicControls.GAP, Y,
                    Component.translatable("key.jamming.menu"), IconButton.MUSIC,
                    () -> client.gui.setScreen(new JammingScreen(screen))));

            // Draw "now playing" next to the buttons, on top of everything else.
            ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, delta) -> {
                Font font = client.font;
                int textX = X + MusicControls.WIDTH + MusicControls.GAP + IconButton.SIZE + 6;
                graphics.text(font, MusicControls.title(), textX, Y + 1, 0xFFFFFFFF, true);
                graphics.text(font, MusicControls.subtitle(), textX, Y + 11, 0xFFA0A0A0, true);
            });

            // Key bindings don't fire while a screen is open, so check the "Open Jamming" key here.
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, key) -> {
                if (JammingKeys.isMenuKey(key)) {
                    client.gui.setScreen(new JammingScreen(screen));
                }
            });
        });
    }
}
