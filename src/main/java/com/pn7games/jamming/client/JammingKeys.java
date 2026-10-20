package com.pn7games.jamming.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.pn7games.jamming.Jamming;
import com.pn7games.jamming.gui.JammingScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;

// Jamming's keys. Players can change them in Options > Controls > Key Binds > Jamming.
public final class JammingKeys {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Jamming.id("main"));

    private static KeyMapping next;
    private static KeyMapping previous;
    private static KeyMapping pause;
    private static KeyMapping loop;
    private static KeyMapping menu;

    private JammingKeys() {
    }

    public static void register() {
        next = key("next", InputConstants.KEY_RIGHT);
        previous = key("previous", InputConstants.KEY_LEFT);
        pause = key("pause", InputConstants.KEY_RSHIFT);
        loop = key("loop", InputConstants.UNKNOWN.getValue()); // not bound by default
        menu = key("menu", InputConstants.KEY_N);

        // Check the keys once per tick. "while" handles several presses in one tick.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (next.consumeClick()) {
                JammingPlayer.next(client);
            }
            while (previous.consumeClick()) {
                JammingPlayer.previous(client);
            }
            while (pause.consumeClick()) {
                JammingPlayer.togglePause(client);
            }
            while (loop.consumeClick()) {
                JammingPlayer.toggleLoop(client);
            }
            while (menu.consumeClick()) {
                client.gui.setScreen(new JammingScreen(null)); // 26.x: screens are opened through Gui
            }
        });
    }

    // True if this key press is the "Open Jamming" key (used in menus, where key bindings don't fire).
    public static boolean isMenuKey(KeyEvent key) {
        return menu.matches(key);
    }

    // Name in the controls menu = lang key "key.jamming.<name>".
    // We use the constructor WITHOUT the key type: it means "keyboard" in every version.
    // (The type was renamed from KEYSYM in 26.2 to KEYBOARD in 26.3, so naming it would break one of them.)
    private static KeyMapping key(String name, int defaultKey) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jamming." + name, defaultKey, CATEGORY));
    }
}
