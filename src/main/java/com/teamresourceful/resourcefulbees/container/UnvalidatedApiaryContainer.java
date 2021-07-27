package com.teamresourceful.resourcefulbees.container;

import com.teamresourceful.resourcefulbees.registry.ModContainers;
import com.teamresourceful.resourcefulbees.tileentity.multiblocks.apiary.ApiaryTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class UnvalidatedApiaryContainer extends Container {

    private final ApiaryTileEntity apiaryTileEntity;
    private final BlockPos pos;

    public UnvalidatedApiaryContainer(int id, World world, BlockPos pos, PlayerInventory inv) {
        super(ModContainers.UNVALIDATED_APIARY_CONTAINER.get(), id);
        this.pos = pos;
        this.apiaryTileEntity = (ApiaryTileEntity)world.getBlockEntity(pos);
    }

    @Override
    public boolean stillValid(@NotNull PlayerEntity playerIn) {
        return true;
    }


    public ApiaryTileEntity getApiaryTileEntity() {
        return apiaryTileEntity;
    }

    public BlockPos getPos() {
        return pos;
    }
}
