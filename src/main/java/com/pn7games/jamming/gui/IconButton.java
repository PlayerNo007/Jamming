package com.pn7games.jamming.gui;

import com.pn7games.jamming.Jamming;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

// A normal vanilla button (same background, hover and press look) with a 9x9 icon instead of text.
// Icons: assets/jamming/textures/gui/sprites/icon/<name>.png (white; tinted here).
public class IconButton extends AbstractButton {

    public static final int SIZE = 20;      // same height as vanilla buttons
    private static final int ICON = 9;
    private static final int ON = 0xFFFFFFFF;
    private static final int OFF = 0xFF8A8A8A; // e.g. Loop while it is off

    public static final Identifier PREVIOUS = Jamming.id("icon/previous");
    public static final Identifier PLAY = Jamming.id("icon/play");
    public static final Identifier PAUSE = Jamming.id("icon/pause");
    public static final Identifier NEXT = Jamming.id("icon/next");
    public static final Identifier LOOP = Jamming.id("icon/loop");
    public static final Identifier MUSIC = Jamming.id("icon/music");

    private final Supplier<Identifier> icon;   // read every frame (play <-> pause)
    private final BooleanSupplier lit;         // false = grey icon
    private final Runnable action;

    public IconButton(int x, int y, Component label, Supplier<Identifier> icon, BooleanSupplier lit, Runnable action) {
        super(x, y, SIZE, SIZE, label);
        this.icon = icon;
        this.lit = lit;
        this.action = action;
        setTooltip(Tooltip.create(label)); // the label is shown on hover and read by the narrator
    }

    public IconButton(int x, int y, Component label, Identifier icon, Runnable action) {
        this(x, y, label, () -> icon, () -> true, action);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractDefaultSprite(graphics); // the vanilla button background
        int x = getX() + (getWidth() - ICON) / 2;
        int y = getY() + (getHeight() - ICON) / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon.get(), x, y, ICON, ICON, lit.getAsBoolean() ? ON : OFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
