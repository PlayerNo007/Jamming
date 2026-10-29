package com.pn7games.jamming.client;

import com.pn7games.jamming.mixin.MusicManagerAccessor;
import com.pn7games.jamming.mixin.SoundManagerAccessor;
import com.pn7games.jamming.music.MusicLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// The player's "brain": which track plays next, history, pause and loop.
// Vanilla MusicManager still decides WHEN music plays; we only decide WHICH track (see MusicManagerMixin).
public final class JammingPlayer {

    private static final int HISTORY_SIZE = 50;
    private static final int AVOID_RECENT = 5; // don't repeat the last 5 tracks if there is a choice

    private static final Deque<Identifier> HISTORY = new ArrayDeque<>();
    private static final RandomSource RANDOM = RandomSource.create();

    @Nullable
    private static Identifier current;
    @Nullable
    private static Identifier requested; // play this one next (from the menu or "previous")
    private static boolean skipLoop;     // "next" was pressed: pick a new track even if loop is on
    private static boolean userChoice;   // the current track was picked by the player (list click / previous)
    private static boolean paused;       // the player pressed pause
    private static boolean suppressed;   // a jukebox / note blocks play nearby (see RecordDetector)
    private static boolean loop;

    private JammingPlayer() {
    }

    // ---- Called by MusicManagerMixin ----

    // Vanilla wants to start "situational" music (e.g. music.game). We swap it for one exact track.
    public static Music chooseTrack(Music situational) {
        Identifier event = situational.sound().value().location();
        Identifier track;
        if (requested != null) {
            track = requested;
            requested = null;
            userChoice = true;
        } else if (loop && !skipLoop && current != null) {
            track = current; // looping keeps "userChoice" as it was
        } else {
            track = pickRandom(MusicLibrary.tracksFor(event));
            userChoice = false;
        }
        skipLoop = false;
        if (track == null) {
            current = null;
            return situational; // event we don't know: let vanilla play it as usual
        }

        paused = false; // a new song never starts paused
        if (!track.equals(HISTORY.peekLast())) {
            HISTORY.addLast(track);
            if (HISTORY.size() > HISTORY_SIZE) {
                HISTORY.removeFirst();
            }
        }
        current = track;
        // A Music that points to ONE file. SoundManagerPreparationsMixin registered every music file
        // as its own sound event, so the game can play it directly.
        Holder<SoundEvent> sound = Holder.direct(SoundEvent.createVariableRangeEvent(track));
        return new Music(sound, situational.minDelay(), situational.maxDelay(), situational.replaceCurrentMusic());
    }

    @Nullable
    private static Identifier pickRandom(Set<Identifier> tracks) {
        if (tracks.isEmpty()) {
            return null;
        }
        List<Identifier> choices = new ArrayList<>(tracks);
        if (choices.size() > 1) {
            // Remove recently played tracks, but always keep at least one choice.
            Iterator<Identifier> recent = HISTORY.descendingIterator();
            for (int i = 0; i < AVOID_RECENT && recent.hasNext() && choices.size() > 1; i++) {
                choices.remove(recent.next());
            }
        }
        return choices.get(RANDOM.nextInt(choices.size()));
    }

    public static boolean isPaused() {
        return paused;
    }

    public static boolean isSuppressed() {
        return suppressed;
    }

    // Music channels must be paused if the player paused OR a jukebox plays nearby.
    public static boolean shouldMusicBePaused() {
        return paused || suppressed;
    }

    public static void setSuppressed(Minecraft client, boolean value) {
        suppressed = value;
        applyPause(client);
    }

    private static void applyPause(Minecraft client) {
        engine(client).jamming$setMusicPaused(shouldMusicBePaused());
    }

    // True while a track the player picked is playing. The situation (menu, underwater, boss...) must not replace it.
    public static boolean isUserChoice(Identifier playing) {
        return userChoice && playing.equals(current);
    }

    public static boolean isLoop() {
        return loop;
    }

    // The track that is playing (or paused) right now, or null (silence between songs, or vanilla music we don't know).
    @Nullable
    public static Identifier currentTrack() {
        boolean musicPlaying = Minecraft.getInstance().getMusicManager().getCurrentMusicTranslationKey() != null;
        return musicPlaying || paused ? current : null;
    }

    // ---- Called by the keys (and later the menu) ----

    public static void next(Minecraft client) {
        skipLoop = true;
        restart(client);
    }

    public static void previous(Minecraft client) {
        if (HISTORY.size() < 2) {
            show(client, Component.translatable("jamming.message.no_previous"));
            return;
        }
        HISTORY.removeLast();               // the current track
        requested = HISTORY.peekLast();     // the one before it
        restart(client);
    }

    public static void play(Minecraft client, Identifier track) {
        requested = track;
        restart(client);
    }

    public static void togglePause(Minecraft client) {
        if (!paused && client.getMusicManager().getCurrentMusicTranslationKey() == null) {
            show(client, Component.translatable("jamming.message.nothing_playing"));
            return;
        }
        paused = !paused;
        if (!paused && current != null && !isMusicSoundAlive(client)) {
            // Vanilla stopped our paused song meanwhile (e.g. dimension change stops all sounds): start it again.
            boolean wasUserChoice = userChoice;
            requested = current;
            restart(client);
            userChoice = wasUserChoice;
        } else {
            applyPause(client);
        }
        show(client, Component.translatable(paused ? "jamming.message.paused" : "jamming.message.resumed"));
    }

    // True if the music manager's song still has a sound channel (playing or paused).
    private static boolean isMusicSoundAlive(Minecraft client) {
        SoundInstance music = ((MusicManagerAccessor) client.getMusicManager()).jamming$getCurrentMusic();
        return music != null && client.getSoundManager().isActive(music);
    }

    public static void toggleLoop(Minecraft client) {
        loop = !loop;
        show(client, Component.translatable(loop ? "jamming.message.loop_on" : "jamming.message.loop_off"));
    }

    // Stop the current track and start a new one right away (vanilla would wait minutes).
    private static void restart(Minecraft client) {
        MusicManager music = client.getMusicManager();
        music.stopPlaying();
        Music situational = client.getSituationalMusic();
        music.startPlaying(situational != null ? situational : Musics.GAME);
        if (suppressed) {
            applyPause(client); // a jukebox still plays nearby: keep the new song paused
        }
    }

    private static MusicPause engine(Minecraft client) {
        return (MusicPause) ((SoundManagerAccessor) client.getSoundManager()).jamming$getSoundEngine();
    }

    // Short message above the hotbar.
    private static void show(Minecraft client, Component message) {
        if (client.player != null) {
            client.player.sendOverlayMessage(message);
        }
    }
}
