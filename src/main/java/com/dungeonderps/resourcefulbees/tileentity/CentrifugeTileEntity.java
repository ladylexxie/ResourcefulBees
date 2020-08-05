
package com.dungeonderps.resourcefulbees.tileentity;

import com.dungeonderps.resourcefulbees.block.CentrifugeBlock;
import com.dungeonderps.resourcefulbees.container.AutomationSensitiveItemStackHandler;
import com.dungeonderps.resourcefulbees.container.CentrifugeContainer;
import com.dungeonderps.resourcefulbees.recipe.CentrifugeRecipe;
import com.dungeonderps.resourcefulbees.registry.RegistryHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CentrifugeTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    public static final int HONEYCOMB_SLOT = 0;
    public static final int BOTTLE_SLOT = 1;

    public static final int HONEY_BOTTLE = 2;
    public static final int OUTPUT1 = 3;
    public static final int OUTPUT2 = 4;

    public AutomationSensitiveItemStackHandler h = new TileStackHandler(5);
    public LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(() -> h);
    public int time = 0;
    public CentrifugeRecipe recipe;
    public ItemStack failedMatch = ItemStack.EMPTY;
    public int totalTime = 0;

    public CentrifugeTileEntity() {
        super(RegistryHandler.CENTRIFUGE_ENTITY.get());
    }


    @Override
    public void tick() {
        if (world != null && !world.isRemote) {
            boolean dirty = false;
            if (!h.getStackInSlot(HONEYCOMB_SLOT).isEmpty() && !h.getStackInSlot(BOTTLE_SLOT).isEmpty()) {
                CentrifugeRecipe irecipe = getRecipe();
                if (this.canProcess(irecipe)) {
                    world.setBlockState(pos,getBlockState().with(CentrifugeBlock.PROPERTY_ON,true));
                    this.totalTime = this.getTime();
                    ++this.time;
                    if (this.time == this.totalTime) {
                        this.time = 0;
                        this.totalTime = this.getTime();
                        this.processItem(irecipe);
                        dirty = true;
                    }
                }
            } else {
                time = 0;
                world.setBlockState(pos,getBlockState().with(CentrifugeBlock.PROPERTY_ON,false));
            }
            if (dirty) {
                this.markDirty();
            }
        }
    }

    protected boolean canProcess(@Nullable CentrifugeRecipe recipe) {
        if (recipe != null) {
            List<Pair<ItemStack, Double>> outputs = recipe.outputs;
            ItemStack glass_bottle = h.getStackInSlot(BOTTLE_SLOT);
            List<ItemStack> outputSlots = new ArrayList<>(
                Arrays.asList(
                    h.getStackInSlot(OUTPUT1),
                    h.getStackInSlot(OUTPUT2),
                    h.getStackInSlot(HONEY_BOTTLE)
                )
            );
            int processScore = 0;
            if (outputSlots.get(0).isEmpty() && outputSlots.get(1).isEmpty() && outputSlots.get(2).isEmpty() && glass_bottle.getItem() == Items.GLASS_BOTTLE) return true;
            else {
                for(int i=0;i<3;i++){
                    if (outputSlots.get(i).isEmpty()) processScore++;
                    else if (outputSlots.get(i).getItem() == outputs.get(i).getLeft().getItem()
                            && outputSlots.get(i).getCount() + outputs.get(i).getLeft().getCount() <= outputSlots.get(i).getMaxStackSize())processScore++;
                }
                if (processScore == 3 && glass_bottle.getItem() == Items.GLASS_BOTTLE)
                    return true;
                else {
                    if (world != null)
                    world.setBlockState(pos,getBlockState().with(CentrifugeBlock.PROPERTY_ON,false));
                    return false;
                }
            }
        }
        return false;
    }

    private void processItem(@Nullable CentrifugeRecipe recipe) {
        if (recipe != null && this.canProcess(recipe)) {
            ItemStack comb = h.getStackInSlot(HONEYCOMB_SLOT);
            ItemStack glass_bottle = h.getStackInSlot(BOTTLE_SLOT);
            List<Pair<ItemStack, Integer>> slots = new ArrayList<>(
                Arrays.asList(
                    Pair.of(h.getStackInSlot(OUTPUT1),OUTPUT1),
                    Pair.of(h.getStackInSlot(OUTPUT2),OUTPUT2),
                    Pair.of(h.getStackInSlot(HONEY_BOTTLE),HONEY_BOTTLE)
                )
            );
            if (world != null)
            for(int i = 0; i < 3; i++){
                Pair<ItemStack, Double> output = recipe.outputs.get(i);
                if (output.getRight() >= world.rand.nextDouble()) {
                    if (slots.get(i).getLeft().isEmpty()) {
                        this.h.setStackInSlot(slots.get(i).getRight(), output.getLeft().copy());
                    } else if (slots.get(i).getLeft().getItem() == output.getLeft().getItem()) {
                        slots.get(i).getLeft().grow(output.getLeft().getCount());
                    }
                    if (slots.get(i).getRight().equals(HONEY_BOTTLE)) glass_bottle.shrink(1);
                }
            }
            comb.shrink(1);
        }
        time = 0;
    }

    protected int getTime() {
        CentrifugeRecipe rec = getRecipe();
        if (rec == null) return 200;
        return rec.time;
    }

    protected CentrifugeRecipe getRecipe() {
        ItemStack input = h.getStackInSlot(HONEYCOMB_SLOT);
        if (input.isEmpty() || input == failedMatch) return null;
        if (world != null)
        if (recipe != null && recipe.matches(new RecipeWrapper(h), world)) return recipe;
        else {
            CentrifugeRecipe rec = world.getRecipeManager().getRecipe(CentrifugeRecipe.CENTRIFUGE_RECIPE_TYPE, new RecipeWrapper(h), this.world).orElse(null);
            if (rec == null) failedMatch = input;
            else failedMatch = ItemStack.EMPTY;
            return recipe = rec;
        }
        return null;
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        CompoundNBT inv = this.h.serializeNBT();
        tag.put("inv", inv);
        tag.putInt("time", time);
        tag.putInt("totalTime", totalTime);
        return super.write(tag);
    }

    @Override
    public void read(@Nonnull BlockState state, CompoundNBT tag) {
        CompoundNBT invTag = tag.getCompound("inv");
        h.deserializeNBT(invTag);
        time = tag.getInt("time");
        totalTime = tag.getInt("totalTime");
        super.read(state, tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? lazyOptional.cast() : super.getCapability(cap, side);
    }

    public AutomationSensitiveItemStackHandler.IAcceptor getAcceptor() {
        return (slot, stack, automation) -> !automation || slot == 0 || slot == 1;
    }

    public AutomationSensitiveItemStackHandler.IRemover getRemover() {
        return (slot, automation) -> !automation || slot == 2 || slot == 3 || slot == 4;
    }

    @Nullable
    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity) {
        //noinspection ConstantConditions
        return new CentrifugeContainer(id, world, pos, playerInventory);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.resourcefulbees.centrifuge");
    }

    protected class TileStackHandler extends AutomationSensitiveItemStackHandler {
        protected TileStackHandler(int slots) {
            super(slots);
        }

        @Override
        public AutomationSensitiveItemStackHandler.IAcceptor getAcceptor() {
            return CentrifugeTileEntity.this.getAcceptor();
        }

        @Override
        public AutomationSensitiveItemStackHandler.IRemover getRemover() {
            return CentrifugeTileEntity.this.getRemover();
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
        }
    }
}

