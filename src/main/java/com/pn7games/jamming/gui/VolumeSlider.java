package com.pn7games.jamming.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

// The game's own "Music" volume (Options > Music & Sounds), as a vanilla slider.
// Drag it, click it, or scroll the mouse wheel over it.
public class VolumeSlider extends AbstractSliderButton {

    private static final double SCROLL_STEP = 0.05; // 5% per wheel notch

    public VolumeSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), musicOption().get());
        updateMessage();
    }

    private static OptionInstance<Double> musicOption() {
        return Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MUSIC);
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.translatable("jamming.screen.volume", Math.round(value * 100)));
    }

    // Setting the option changes the real volume right away (vanilla listens to it).
    @Override
    protected void applyValue() {
        musicOption().set(value);
    }

    // Save to options.txt when the player lets go (not on every pixel of dragging).
    @Override
    public void onRelease(MouseButtonEvent event) {
        super.onRelease(event);
        Minecraft.getInstance().options.save();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        setValue(value + Math.signum(scrollY) * SCROLL_STEP); // setValue clamps to 0..1
        Minecraft.getInstance().options.save();
        return true;
    }
}
