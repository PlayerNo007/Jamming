package com.pn7games.jamming.gui;

import com.pn7games.jamming.client.JammingPlayer;
import com.pn7games.jamming.music.TrackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

// Things both menus share: the 4 play buttons and the "now playing" text.
public final class MusicControls {

    public static final int GAP = 4;
    public static final int WIDTH = 4 * IconButton.SIZE + 3 * GAP; // width of the 4 buttons together

    private MusicControls() {
    }

    // previous | play/pause | next | loop
    public static List<IconButton> create(Minecraft client, int x, int y) {
        int step = IconButton.SIZE + GAP;
        return List.of(
                new IconButton(x, y, Component.translatable("key.jamming.previous"), IconButton.PREVIOUS,
                        () -> JammingPlayer.previous(client)),
                new IconButton(x + step, y, Component.translatable("key.jamming.pause"),
                        () -> JammingPlayer.isPaused() ? IconButton.PLAY : IconButton.PAUSE, () -> true,
                        () -> JammingPlayer.togglePause(client)),
                new IconButton(x + 2 * step, y, Component.translatable("key.jamming.next"), IconButton.NEXT,
                        () -> JammingPlayer.next(client)),
                new IconButton(x + 3 * step, y, Component.translatable("key.jamming.loop"), () -> IconButton.LOOP,
                        JammingPlayer::isLoop, () -> JammingPlayer.toggleLoop(client)));
    }

    // "Title" and "Artist · Group" of the playing track, or a "silence" text.
    public static Component title() {
        Identifier current = JammingPlayer.currentTrack();
        return current == null
                ? Component.translatable("jamming.screen.nothing")
                : Component.literal(TrackInfo.of(current).title());
    }

    public static Component subtitle() {
        Identifier current = JammingPlayer.currentTrack();
        if (current == null) {
            return Component.empty();
        }
        TrackInfo info = TrackInfo.of(current);
        String text = info.artist().isEmpty() ? info.dimensionLabel() : info.artist() + "  ·  " + info.dimensionLabel();
        if (JammingPlayer.isSuppressed()) {
            return Component.literal(text + "  ·  ").append(Component.translatable("jamming.screen.tag.jukebox"));
        }
        if (JammingPlayer.isPaused()) {
            return Component.literal(text + "  ·  ").append(Component.translatable("jamming.screen.tag.paused"));
        }
        return Component.literal(text);
    }
}
