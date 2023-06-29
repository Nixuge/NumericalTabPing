package me.nixuge.numericaltabping.mixins.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin {
    FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

    // Explanation:
    // I couldn't simply replace the drawPing method as the padding isn't the same.
    // So what I've done instead to replace the padding:
    // - Set a flag using a call before the part that calculates the width
    // - Have the padding get calculated with the width of the logo removed and the width of "100ms" added
    //// Note that there's also another call right after, so the redirected function sets a flag once
    //// it's called at first to avoid having it run 2x if there's another call right after
    // - Disable the flag using a call after the part that calculates the width, so that other calls
    //// to that function later wouldn't get disrupted.
    private boolean shouldAddMsSizeToWidth = false;
    private boolean processedFirstCall = false;
    @Inject(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    public void beforeStart(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        this.shouldAddMsSizeToWidth = true;
        this.processedFirstCall = false;
    }
    @Inject(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    public void afterStart(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        this.shouldAddMsSizeToWidth = false;
    }
    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I"))
    public int redirectStringWidth(FontRenderer instance, String text) {
        if (!shouldAddMsSizeToWidth || processedFirstCall) {
            return fontRenderer.getStringWidth(text);
        }
        processedFirstCall = true;
        // Note: this technically sometimes adds more padding than needed and rarely not enough,
        // but I can't do better without rewriting huge parts of the original function,
        return fontRenderer.getStringWidth(text + "100ms") + 14;
        // Note2: To match vanilla I should remove 14 here (the already present ping image padding)
        // But adding 14 instead makes the tab a bit wider (which is enjoyable) and greatly reduces overlap
        // (at least from some tests on Hypixel's Bedwars Lobby #1)
    }

    /**
     * @author Nixuge
     * @reason We just want to overwrite the entire ping drawing
     */
    @Overwrite
    public void drawPing(int xOffsetLeft, int x, int y, NetworkPlayerInfo networkPlayerInfoIn) {
        int ping = networkPlayerInfoIn.getResponseTime();

        String finalString = ping + "ms";
        int xOffsetRight = fontRenderer.getStringWidth(finalString);

        int pingColor;
        if (ping < 150)
            pingColor = -16711936;
        else if (ping < 300)
            pingColor = -256;
        else if (ping < 600)
            pingColor = -14336;
        else if (ping < 1000)
            pingColor = -65536;
        else
            pingColor = -8355712;

        fontRenderer.drawStringWithShadow(finalString, x - xOffsetRight + xOffsetLeft, y, pingColor);
    }
}
