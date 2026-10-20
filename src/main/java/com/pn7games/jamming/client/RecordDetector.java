package com.pn7games.jamming.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// Pauses Jamming's music while a jukebox or note blocks play near the player, and resumes it afterwards.
// Both jukeboxes and note blocks play their sounds in the RECORDS sound category ("Jukebox/Note Blocks" slider).
public final class RecordDetector implements SoundEventListener {

    private static final double RADIUS = 32.0;        // blocks
    private static final int NOTE_QUIET_TICKS = 5 * 20; // note blocks: resume after 5 s without a note

    private final Set<SoundInstance> records = new HashSet<>(); // long sounds (jukebox songs) we are watching
    private int ticksSinceNote = Integer.MAX_VALUE;

    // Called by the sound engine for EVERY sound that starts playing.
    @Override
    public void onPlaySound(SoundInstance sound, WeighedSoundEvents event, float range) {
        if (sound.getSource() != SoundSource.RECORDS || !isNear(sound)) {
            return;
        }
        records.add(sound);
        ticksSinceNote = 0;
    }

    // Called every client tick (see JammingClient).
    public void tick(Minecraft client) {
        if (ticksSinceNote < Integer.MAX_VALUE) {
            ticksSinceNote++;
        }
        // Forget sounds that stopped or are now too far away (the player walked off).
        Iterator<SoundInstance> it = records.iterator();
        while (it.hasNext()) {
            SoundInstance sound = it.next();
            if (!client.getSoundManager().isActive(sound) || !isNear(sound)) {
                it.remove();
            }
        }

        boolean recordNearby = JammingConfig.pauseForJukebox()
                && client.level != null
                && (!records.isEmpty() || ticksSinceNote < NOTE_QUIET_TICKS);
        if (recordNearby != JammingPlayer.isSuppressed()) {
            JammingPlayer.setSuppressed(client, recordNearby);
            if (client.player != null) {
                client.player.sendOverlayMessage(Component.translatable(
                        recordNearby ? "jamming.message.record_pause" : "jamming.message.record_resume"));
            }
        }
    }

    private static boolean isNear(SoundInstance sound) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || sound.isRelative()) {
            return false; // relative sounds play "at the ears" (UI), not in the world
        }
        Vec3 position = new Vec3(sound.getX(), sound.getY(), sound.getZ());
        return client.player.position().distanceToSqr(position) <= RADIUS * RADIUS;
    }
}
