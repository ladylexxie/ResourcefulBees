package com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.BeepediaPage;
import com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.stats.CombBeepediaStats;
import com.teamresourceful.resourcefulbees.client.gui.widget.ScreenArea;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CombPage extends BeepediaPage {

    private CombBeepediaStats stats;

    public CombPage(ScreenArea screenArea) {
        super(screenArea);
    }

    public void preInit(CombBeepediaStats stats) {
        this.stats = stats;
    }

    public static void initPages() {

    }

    @Override
    public void renderBackground(PoseStack matrix, float partialTick, int mouseX, int mouseY) {

    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    @Override
    public void addSearch(BeepediaPage parent) {

    }
}
