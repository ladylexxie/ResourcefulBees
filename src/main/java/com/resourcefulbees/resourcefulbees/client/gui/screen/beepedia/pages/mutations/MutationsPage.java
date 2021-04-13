package com.resourcefulbees.resourcefulbees.client.gui.screen.beepedia.pages.mutations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.api.beedata.CustomBeeData;
import com.resourcefulbees.resourcefulbees.client.gui.screen.beepedia.BeepediaPage;
import com.resourcefulbees.resourcefulbees.client.gui.screen.beepedia.BeepediaScreen;
import com.resourcefulbees.resourcefulbees.entity.passive.CustomBeeEntity;
import com.resourcefulbees.resourcefulbees.lib.MutationTypes;
import com.resourcefulbees.resourcefulbees.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.resourcefulbees.resourcefulbees.client.gui.screen.beepedia.BeepediaPage.SUB_PAGE_WIDTH;

public abstract class MutationsPage {
    protected final BeepediaScreen beepedia;
    Entity parent;
    MutationTypes type;
    CustomBeeData beeData;
    protected int inputCounter;
    protected int outputCounter;
    protected final ResourceLocation infoIcon = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/gui/jei/icons.png");

    protected MutationsPage(Entity parent, MutationTypes type, CustomBeeData beeData, BeepediaScreen beepedia) {
        this.parent = parent;
        this.type = type;
        this.beeData = beeData;
        this.beepedia = beepedia;
    }

    public abstract void tick(int ticksActive);

    public void draw(MatrixStack matrix, int xPos, int yPos) {
        RenderUtils.renderEntity(matrix, parent, beepedia.getMinecraft().level, (float) xPos + ((float) SUB_PAGE_WIDTH / 2) - 15, (float) yPos + 6, 45, 1.25f);
    }

    public boolean mouseClick(int xPos, int yPos, int mouseX, int mouseY) {
        if (parent instanceof CustomBeeEntity) {
            CustomBeeEntity beeEntity = (CustomBeeEntity) parent;
            if (BeepediaScreen.mouseHovering((float) xPos + ((float) BeepediaPage.SUB_PAGE_WIDTH / 2) - 20, (float) yPos + 6, 30, 30, mouseX, mouseY)) {
                if (BeepediaScreen.currScreenState.getPageID().equals((beeEntity.getBeeData().getName()))) return false;
                BeepediaScreen.saveScreenState();
                beepedia.setActive(BeepediaScreen.PageType.BEE, beeEntity.getBeeData().getName());
                return true;
            }
        }
        return false;
    }

    public void drawTooltips(MatrixStack matrix, int xPos, int yPos, int mouseX, int mouseY) {
        if (BeepediaScreen.mouseHovering((float) xPos + ((float) BeepediaPage.SUB_PAGE_WIDTH / 2) - 20, (float) yPos + 6, 30, 30, mouseX, mouseY)) {
            List<ITextComponent> tooltip = new ArrayList<>();
            IFormattableTextComponent name = parent.getName().plainCopy();
            IFormattableTextComponent id = new StringTextComponent(parent.getEncodeId()).withStyle(TextFormatting.DARK_GRAY);
            tooltip.add(name);
            tooltip.add(id);
            beepedia.renderComponentTooltip(matrix, tooltip, mouseX, mouseY);
        }
    }

    protected void drawWeight(MatrixStack matrix, Double right, int xPos, int yPos) {
        FontRenderer font = Minecraft.getInstance().font;
        DecimalFormat decimalFormat = new DecimalFormat("##%");
        StringTextComponent text = new StringTextComponent(decimalFormat.format(right));
        int padding = font.width(text) / 2;
        font.draw(matrix, text.withStyle(TextFormatting.GRAY), (float) xPos - padding, yPos, -1);
    }

    protected void drawChance(MatrixStack matrix, Double right, int xPos, int yPos) {
        if (right >= 1) return;
        drawWeight(matrix, right, xPos, yPos);
    }


    public abstract String getSearch();
}
