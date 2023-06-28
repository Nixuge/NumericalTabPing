package me.nixuge.numericaltabping.mixins;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerInfo;

import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GuiIngameForge.class)
public class GuiInGameForgeMixin {

    @Shadow
    private FontRenderer fontrenderer;

    // Quick note:
    // Inside renderPlayerList is a line that goes:
    // if (pre(PLAYER_LIST)) return;
    // That means if a "pre" event is registered, this won't render at all
    //
    // Normal Forge behavior, but still worth nothing in my opinion, especially
    // if anyone tries to debug this without much knowledge

    private GuiPlayerInfo currentPlayer;
    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"), remap = false)
    protected Object grabCurrentPlayerInLoop(List<GuiPlayerInfo> instance, int i) {
        this.currentPlayer = instance.get(i);
        return this.currentPlayer;
    }

    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    protected void drawPlayerPing(GuiIngameForge instance, int x, int y, int a, int b, int c, int d) {
        int ping = currentPlayer.responseTime;
        int pingColor;

        // System.out.println(Color.GREEN.getRGB() + " " + Color.YELLOW.getRGB() + " " + Color.ORANGE.getRGB() + " " + Color.RED.getRGB() + " " + Color.GRAY.getRGB());
        if (ping < 150) pingColor = -16711936;
        else if (ping < 300) pingColor = -256;
        else if (ping < 600) pingColor = -14336;
        else if (ping < 1000) pingColor = -65536;
        else pingColor = -8355712;

        String drawString = ping + "ms";
        int xOffset = fontrenderer.getStringWidth(drawString) - 10;

        fontrenderer.drawStringWithShadow(drawString, x - xOffset, y, pingColor);
    }
}
