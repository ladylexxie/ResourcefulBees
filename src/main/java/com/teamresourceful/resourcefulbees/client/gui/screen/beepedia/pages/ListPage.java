package com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.pages;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.*;
import com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.enums.BeepediaListTypes;
import com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.stats.BeepediaStats;
import com.teamresourceful.resourcefulbees.client.gui.widget.*;
import com.teamresourceful.resourcefulbees.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import software.bernie.shadowed.eliotlash.mclib.math.functions.limit.Min;

import java.util.Map;

public class ListPage extends BeepediaPage {

    private ToggleItemImageButton bees;
    private ToggleItemImageButton traits;
    private ToggleItemImageButton honey;
    private ToggleItemImageButton combs;

    public ListPage(BeepediaScreenArea screenArea) {
        super(screenArea);
    }

    private boolean isActive(BeepediaListTypes type) {
        return BeepediaState.currentState.selectedList == type;
    }

    @Override
    public void tick(int ticksActive) {
        super.tick(ticksActive);
        BeepediaListTypes type = BeepediaState.currentState.selectedList;
        bees.enabled = type == BeepediaListTypes.BEES;
        traits.enabled = type == BeepediaListTypes.TRAITS;
        honey.enabled = type == BeepediaListTypes.HONEY;
        combs.enabled = type == BeepediaListTypes.COMBS;
        bees.active = type != BeepediaListTypes.BEES;
        traits.active = type != BeepediaListTypes.TRAITS;
        honey.active = type != BeepediaListTypes.HONEY;
        combs.active = type != BeepediaListTypes.COMBS;
    }

    private void updateState(BeepediaListTypes type) {
        BeepediaState.updateState(type, null, null, null, null);
    }

    private ToggleItemImageButton createButton(int xOffset, int yOffset, ButtonTemplate template, BeepediaListTypes type, ITextComponent tooltip, Item item) {
        return new ToggleItemImageButton(xOffset, yOffset, 0, isActive(type),
                template, button -> updateState(type), tooltip, 2, 2, new ItemStack(item));
    }

    @Override
    public void registerScreen(BeepediaScreen beepedia) {
        super.registerScreen(beepedia);
        int x = getXPos() + 46;
        int y = getYPos() + 8;
        ButtonTemplate template = new ButtonTemplate(20, 20, 0, 0, 20, 20, 60, BeepediaImages.BUTTON_IMAGE);
        bees = createButton(x, y, template, BeepediaListTypes.BEES, BeepediaLang.TAB_BEES, Items.BEEHIVE);
        traits = createButton(x + 21, y, template, BeepediaListTypes.TRAITS, BeepediaLang.TAB_TRAITS, ModItems.TRAIT_ICON.get());
        honey = createButton(x + 42, y, template, BeepediaListTypes.HONEY, BeepediaLang.TAB_HONEY, Items.HONEY_BOTTLE);
        combs = createButton(x + 63, y, template, BeepediaListTypes.COMBS, BeepediaLang.TAB_COMBS, Items.HONEYCOMB);
        pageButtons.add(bees);
        pageButtons.add(traits);
        pageButtons.add(honey);
        pageButtons.add(combs);
        beepedia.addButtons(pageButtons);
    }

    @Override
    public void renderBackground(MatrixStack matrix, float partialTick, int mouseX, int mouseY) {
        ITextComponent title;
        FontRenderer font = Minecraft.getInstance().font;
        switch (BeepediaState.currentState.selectedList) {
            case COMBS:
                title = BeepediaLang.TAB_COMBS;
                break;
            case HONEY:
                title = BeepediaLang.TAB_HONEY;
                break;
            case TRAITS:
                title = BeepediaLang.TAB_TRAITS;
                break;
            default:
                title = BeepediaLang.TAB_BEES;
                break;
        }
        font.draw(matrix, title, getXPos() + 10.0f, getYPos() + 20.0f, 16777215);
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {

    }

    @Override
    public void drawTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {

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
