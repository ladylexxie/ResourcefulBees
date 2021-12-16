package com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.containers;

import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.blocks.CentrifugeFluidOutput;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.entities.CentrifugeFluidOutputEntity;
import com.teamresourceful.resourcefulbees.common.registry.minecraft.ModContainers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.NotNull;

public class CentrifugeFluidOutputContainer extends CentrifugeContainer<CentrifugeFluidOutputEntity> {

    public CentrifugeFluidOutputContainer(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(id, inv, getTileFromBuf(inv.player.level, buffer, CentrifugeFluidOutputEntity.class));
    }

    public CentrifugeFluidOutputContainer(int id, Inventory inv, CentrifugeFluidOutputEntity entity) {
        super(ModContainers.CENTRIFUGE_FLUID_OUTPUT_CONTAINER.get(), id, inv, entity);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return entity != null && ContainerLevelAccess.create(level, entity.getBlockPos()).evaluate((world, pos) ->
                world.getBlockState(pos).getBlock() instanceof CentrifugeFluidOutput && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D, true);
    }

    @Override
    public int getContainerInputEnd() {
        return 0;
    }

    @Override
    public int getInventoryStart() {
        return 0;
    }

    @Override
    public int getContainerInputStart() {
        return 0;
    }

    @Override
    protected void setupSlots() {
        addPlayerInvSlots();
    }
}
