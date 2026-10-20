package com.pn7games.jamming.client;

import com.pn7games.jamming.gui.TitleScreenPlayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class JammingClient implements ClientModInitializer {

    private static final RecordDetector RECORD_DETECTOR = new RecordDetector();

    @Override
    public void onInitializeClient() {
        JammingConfig.load();
        JammingKeys.register();
        TitleScreenPlayer.register();

        // The sound manager is ready once the client has started.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> client.getSoundManager().addListener(RECORD_DETECTOR));
        ClientTickEvents.END_CLIENT_TICK.register(RECORD_DETECTOR::tick);
    }
}
