package com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.entities;

import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.containers.CentrifugeTerminalContainer;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.entities.base.AbstractGUICentrifugeEntity;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.helpers.CentrifugeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeTerminalEntity extends AbstractGUICentrifugeEntity {

    public CentrifugeTerminalEntity(RegistryObject<BlockEntityType<CentrifugeTerminalEntity>> entityType, CentrifugeTier tier, BlockPos pos, BlockState state) {
        super(entityType.get(), tier, pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.centrifuge.terminal." + tier.getName());
    }

    //TODO see if this can be handled generically via the superclass AbstractGUICentrifugeEntity
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
        return new CentrifugeTerminalContainer(id, playerInventory, this, centrifugeState);
    }
}
