package com.teamresourceful.resourcefulbees.tileentity.multiblocks.apiary;

import com.teamresourceful.resourcefulbees.block.multiblocks.apiary.ApiaryBlock;
import com.teamresourceful.resourcefulbees.block.multiblocks.apiary.ApiaryBreederBlock;
import com.teamresourceful.resourcefulbees.block.multiblocks.apiary.ApiaryStorageBlock;
import com.teamresourceful.resourcefulbees.container.UnvalidatedApiaryContainer;
import com.teamresourceful.resourcefulbees.container.ValidatedApiaryContainer;
import com.teamresourceful.resourcefulbees.lib.constants.NBTConstants;
import com.teamresourceful.resourcefulbees.lib.enums.ApiaryTab;
import com.teamresourceful.resourcefulbees.network.NetPacketHandler;
import com.teamresourceful.resourcefulbees.network.packets.UpdateClientApiaryMessage;
import com.teamresourceful.resourcefulbees.registry.ModBlocks;
import com.teamresourceful.resourcefulbees.tileentity.multiblocks.MultiBlockHelper;
import com.teamresourceful.resourcefulbees.utils.BeeInfoUtils;
import com.teamresourceful.resourcefulbees.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApiaryController extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IApiaryMultiblock {

    private final List<BlockPos> structureBlocks = new ArrayList<>();
    protected boolean isValidApiary;
    private boolean previewed;
    private int horizontalOffset = 0;
    private int verticalOffset = 0;
    private int width = 7;
    private int height = 6;
    private int depth = 7;
    private int numPlayersUsing;
    private int ticksSinceValidation;
    private int ticksSinceSync;
    private BlockPos storagePos;
    private BlockPos breederPos;
    private ApiaryStorageTileEntity apiaryStorage;
    private ApiaryBreederTileEntity apiaryBreeder;


    public ApiaryController(TileEntityType<?> entityType) {
        super(entityType);
    }

    //region PLAYER SYNCING
    public static int calculatePlayersUsingSync(World world, ApiaryController apiaryTileEntity, int ticksSinceSync, int posX, int posY, int posZ, int numPlayersUsing) {
        if (!world.isClientSide && numPlayersUsing != 0 && (ticksSinceSync + posX + posY + posZ) % 200 == 0) {
            numPlayersUsing = calculatePlayersUsing(world, apiaryTileEntity, posX, posY, posZ);
        }

        return numPlayersUsing;
    }

    public static int calculatePlayersUsing(World world, ApiaryController apiaryTileEntity, int posX, int posY, int posZ) {
        int i = 0;
        float f = 5.0F;

        for (PlayerEntity playerentity : world.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB(posX - f, posY - f, posZ - f, (posX + 1) + f, (posY + 1) + f, (posZ + 1) + f))) {
            if (playerentity.containerMenu instanceof ValidatedApiaryContainer) {
                ApiaryTileEntity apiaryTileEntity1 = ((ValidatedApiaryContainer) playerentity.containerMenu).getApiaryTileEntity();
                if (apiaryTileEntity1 == apiaryTileEntity) {
                    ++i;
                }
            }
        }

        return i;
    }

    public static void syncApiaryToPlayersUsing(World world, BlockPos pos, CompoundNBT data) {
        NetPacketHandler.sendToAllLoaded(new UpdateClientApiaryMessage(pos, data), world, pos);
    }
    //endregion

    public boolean isValidApiary(boolean runValidation) {
        if (runValidation) {
            runStructureValidation(null);
        }
        return isValidApiary;
    }

    public ApiaryStorageTileEntity getApiaryStorage() {
        if (level != null && getStoragePos() != null) {
            TileEntity tile = level.getBlockEntity(getStoragePos());
            if (tile instanceof ApiaryStorageTileEntity) {
                return (ApiaryStorageTileEntity) tile;
            }
        }
        setStoragePos(null);
        return null;
    }

    public ApiaryBreederTileEntity getApiaryBreeder() {
        if (level != null && getBreederPos() != null) {
            TileEntity tile = level.getBlockEntity(getBreederPos());
            if (tile instanceof ApiaryBreederTileEntity) {
                return (ApiaryBreederTileEntity) tile;
            }
        }
        setBreederPos(null);
        return null;
    }

    public void runStructureValidation(@Nullable ServerPlayerEntity validatingPlayer) {
        if (this.level != null && !this.level.isClientSide()) {
            if (!this.isValidApiary || structureBlocks.isEmpty())
                buildStructureBlockList();
            this.isValidApiary = validateStructure(this.level, validatingPlayer);
            this.level.setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(ApiaryBlock.VALIDATED, this.isValidApiary));
            if (validatingPlayer != null && this.isValidApiary) {
                NetworkHooks.openGui(validatingPlayer, this, this.getBlockPos());
            }
            this.ticksSinceValidation = 0;
        }
    }

    public boolean validateStructure(World worldIn, @Nullable ServerPlayerEntity validatingPlayer) {
        AtomicBoolean isStructureValid = new AtomicBoolean(true);
        this.apiaryStorage = getApiaryStorage();
        this.apiaryBreeder = getApiaryBreeder();
        validateLinks();
        isStructureValid.set(validateBlocks(isStructureValid, worldIn, validatingPlayer));

        if (apiaryStorage == null) {
            isStructureValid.set(false);
            if (validatingPlayer != null) {
                validatingPlayer.displayClientMessage(new StringTextComponent("Missing Apiary Storage Block!"), false);
            }
        }

        if (validatingPlayer != null) {
            validatingPlayer.displayClientMessage(new TranslationTextComponent("gui.resourcefulbees.apiary.validated." + isStructureValid.get()), true);
        }
        return isStructureValid.get();
    }

    private boolean validateBlocks(AtomicBoolean isStructureValid, World worldIn, @Nullable ServerPlayerEntity validatingPlayer) {
        structureBlocks.forEach(pos -> {
            Block block = worldIn.getBlockState(pos).getBlock();
            if (block.is(BeeInfoUtils.getValidApiaryTag()) || block instanceof ApiaryBreederBlock || block instanceof ApiaryStorageBlock || block instanceof ApiaryBlock) {
                TileEntity tile = worldIn.getBlockEntity(pos);
                linkStorageAndBreeder(tile);
            } else {
                isStructureValid.set(false);
                if (validatingPlayer != null)
                    validatingPlayer.displayClientMessage(new StringTextComponent(String.format("Block at position (X: %1$s Y: %2$s Z: %3$s) is invalid!", pos.getX(), pos.getY(), pos.getZ())), false);
            }
        });

        return isStructureValid.get();
    }


    public MutableBoundingBox buildStructureBounds(int horizontalOffset, int verticalOffset) {
        return MultiBlockHelper.buildStructureBounds(this.getBlockPos(), width, height, depth, getAdjustedHOffset(horizontalOffset), -verticalOffset - 2, 0, this.getBlockState().getValue(ApiaryBlock.FACING));
    }

    private int getAdjustedHOffset(int horizontalOffset) {
        return -horizontalOffset - 3  - (int) ((width - 7) * 0.5);
    }

    private void buildStructureBlockList() {
        if (this.level != null) {
            MutableBoundingBox box = buildStructureBounds(this.getHorizontalOffset(), this.getVerticalOffset());
            structureBlocks.clear();
            BlockPos.betweenClosedStream(box).forEach((blockPos -> {
                if (blockPos.getX() == box.x0 || blockPos.getX() == box.x1 ||
                        blockPos.getY() == box.y0 || blockPos.getY() == box.y1 ||
                        blockPos.getZ() == box.z0 || blockPos.getZ() == box.z1) {
                    structureBlocks.add(blockPos.immutable());
                }
            }));
        }
    }

    public void runCreativeBuild(ServerPlayerEntity player) {
        if (this.level != null) {
            buildStructureBlockList();
            boolean addedStorage = false;
            for (BlockPos pos : structureBlocks) {
                Block block = this.level.getBlockState(pos).getBlock();
                if (!(block instanceof ApiaryBlock)) {
                    if (addedStorage) {
                        this.level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
                    } else {
                        this.level.setBlockAndUpdate(pos, ModBlocks.APIARY_STORAGE_BLOCK.get().defaultBlockState());
                        addedStorage = true;
                    }
                }
            }
            runStructureValidation(player);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean linkStorageAndBreeder(TileEntity tile) {
        if (tile instanceof ApiaryStorageTileEntity && apiaryStorage == null && ((ApiaryStorageTileEntity) tile).getApiaryPos() == null) {
            apiaryStorage = (ApiaryStorageTileEntity) tile;
            setStoragePos(apiaryStorage.getBlockPos());
            apiaryStorage.setApiaryPos(this.worldPosition);
            if (level != null) {
                level.sendBlockUpdated(getStoragePos(), apiaryStorage.getBlockState(), apiaryStorage.getBlockState(), 2);
            }
            return true;
        }

        if (tile instanceof ApiaryBreederTileEntity && apiaryBreeder == null && ((ApiaryBreederTileEntity) tile).getApiaryPos() == null) {
            apiaryBreeder = (ApiaryBreederTileEntity) tile;
            setBreederPos(apiaryBreeder.getBlockPos());
            apiaryBreeder.setApiaryPos(this.worldPosition);
            if (level != null) {
                level.sendBlockUpdated(getBreederPos(), apiaryBreeder.getBlockState(), apiaryBreeder.getBlockState(), 2);
            }
            return true;
        }

        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean validateLinks() {
        boolean linksValid = true;
        if (apiaryStorage != null && (apiaryStorage.getApiaryPos() == null || positionsMismatch(apiaryStorage.getApiaryPos()))) {
            apiaryStorage = null;
            setStoragePos(null);
            linksValid = false;
        }
        if (apiaryBreeder != null && (apiaryBreeder.getApiaryPos() == null || positionsMismatch(apiaryBreeder.getApiaryPos()))) {
            apiaryBreeder = null;
            setBreederPos(null);
            linksValid = false;
        }
        if (!linksValid) {
            setChanged();
        }
        return linksValid;
    }

    private boolean positionsMismatch(BlockPos pos) {
        return pos.compareTo(this.worldPosition) != 0;
    }

    public int getHorizontalOffset() {
        return horizontalOffset;
    }

    /**
     * Sets the horizontal offset for the apiary block entity in relationship to the apiary structure.
     * This value makes an assumption that the apiary is starting from the center-most position in the structure face
     * and that the apiary cannot be placed at the edges.
     *
     * Example: 7-wide face, apiary is at canter position with a value range of -2/+2
     * 14-wide face, apiary is starting at 7 blocks from right with a value range of -5/+6
     *
     * @param horizontalOffset Sets the horizontal offset for the apiary block in relationship to the apiary structure.
     */
    public void setHorizontalOffset(int horizontalOffset) {
        this.horizontalOffset = horizontalOffset;
    }

    public int getVerticalOffset() {
        return verticalOffset;
    }

    /**
     * Sets the vertical offset for the apiary block entity in relationship to the apiary structure.
     * This value makes an assumption that the apiary is starting two blocks up from the bottom most block
     * and that the apiary cannot be placed at the edges.
     *
     * Example: 6-high face, apiary is two blocks up from bottom with a value range of -1/+2
     * 10-high face, apiary is starting at 2 blocks up from bottom with a value range of -1/+6
     *
     * @param verticalOffset Sets the vertical offset for the apiary block entity in relationship to the apiary structure.
     */
    public void setVerticalOffset(int verticalOffset) {
        this.verticalOffset = verticalOffset;
    }

    public int getNumPlayersUsing() {
        return numPlayersUsing;
    }

    public void setNumPlayersUsing(int numPlayersUsing) {
        this.numPlayersUsing = numPlayersUsing;
    }

    public boolean isPreviewed() {
        return previewed;
    }

    public void setPreviewed(boolean previewed) {
        this.previewed = previewed;
    }

    public BlockPos getStoragePos() {
        return storagePos;
    }

    public void setStoragePos(BlockPos storagePos) {
        this.storagePos = storagePos;
    }

    public BlockPos getBreederPos() {
        return breederPos;
    }

    public void setBreederPos(BlockPos breederPos) {
        this.breederPos = breederPos;
    }

    /**
     * Returns the width of the apiary structure.
     *
     * @return Width of the apiary structure.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the apiary structure.
     * Minimum value is 7.
     * Maximum value is 16.
     *
     * @param width Sets the width of the apiary structure.
     */
    public void setWidth(int width) {
        this.width = MathUtils.clamp(width, 7, 16);
    }

    /**
     * Returns the height of the apiary structure.
     *
     * @return Height of the apiary structure.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the apiary structure.
     * Minimum value is 6.
     * Maximum value is 10.
     *
     * @param height Sets the width of the apiary structure.
     */
    public void setHeight(int height) {
        this.height = MathUtils.clamp(height, 6, 10);
    }

    /**
     * Returns the depth of the apiary structure.
     *
     * @return Depth of the apiary structure.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth of the apiary structure.
     * Minimum value is 7.
     * Maximum value is 16.
     *
     * @param depth Sets the width of the apiary structure.
     */
    public void setDepth(int depth) {
        this.depth = MathUtils.clamp(depth, 7, 16);
    }

    @Nullable
    @Override
    public Container createMenu(int i, @NotNull PlayerInventory playerInventory, @NotNull PlayerEntity playerEntity) {
        if (level != null) {
            setNumPlayersUsing(getNumPlayersUsing() + 1);
            if (isValidApiary(true)) {
                return new ValidatedApiaryContainer(i, level, worldPosition, playerInventory);
            }
            return new UnvalidatedApiaryContainer(i, level, worldPosition, playerInventory);
        }
        return null;
    }

    @Override
    public void switchTab(ServerPlayerEntity player, ApiaryTab tab) {
        if (level != null) {
            if (tab == ApiaryTab.STORAGE) {
                NetworkHooks.openGui(player, getApiaryStorage(), getStoragePos());
            }
            if (tab == ApiaryTab.BREED) {
                NetworkHooks.openGui(player, getApiaryBreeder(), getBreederPos());
            }
        }
    }

    @Override
    public void tick() {
        if (level != null) {
            BlockPos blockpos = this.getBlockPos();
            int x = blockpos.getX();
            int y = blockpos.getY();
            int z = blockpos.getZ();
            ++ticksSinceSync;
            this.setNumPlayersUsing(calculatePlayersUsingSync(this.level, this, this.ticksSinceSync, x, y, z, this.getNumPlayersUsing()));

            if (!level.isClientSide && isValidApiary) {
                if (ticksSinceValidation >= 20) runStructureValidation(null);
                else ticksSinceValidation++;
            }
        }
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.resourcefulbees.apiary");
    }

    @Override
    public void load(@NotNull BlockState state, @NotNull CompoundNBT nbt) {
        super.load(state, nbt);
        this.loadFromNBT(nbt);
    }

    @NotNull
    @Override
    public CompoundNBT save(@NotNull CompoundNBT nbt) {
        super.save(nbt);
        return this.saveToNBT(nbt);
    }

    public void loadFromNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.NBT_VALID_APIARY))
            this.isValidApiary = nbt.getBoolean(NBTConstants.NBT_VALID_APIARY);
        if (nbt.contains(NBTConstants.NBT_VERT_OFFSET))
            this.setVerticalOffset(nbt.getInt(NBTConstants.NBT_VERT_OFFSET));
        if (nbt.contains(NBTConstants.NBT_HOR_OFFSET))
            this.setHorizontalOffset(nbt.getInt(NBTConstants.NBT_HOR_OFFSET));
        if (nbt.contains(NBTConstants.NBT_WIDTH))
            this.setWidth(nbt.getInt(NBTConstants.NBT_WIDTH));
        if (nbt.contains(NBTConstants.NBT_HEIGHT))
            this.setHeight(nbt.getInt(NBTConstants.NBT_HEIGHT));
        if (nbt.contains(NBTConstants.NBT_DEPTH))
            this.setDepth(nbt.getInt(NBTConstants.NBT_DEPTH));
        if (nbt.contains(NBTConstants.NBT_STORAGE_POS))
            setStoragePos(NBTUtil.readBlockPos(nbt.getCompound(NBTConstants.NBT_STORAGE_POS)));
        if (nbt.contains(NBTConstants.NBT_BREEDER_POS))
            setBreederPos(NBTUtil.readBlockPos(nbt.getCompound(NBTConstants.NBT_BREEDER_POS)));
    }

    public CompoundNBT saveToNBT(CompoundNBT nbt) {
        nbt.putBoolean(NBTConstants.NBT_VALID_APIARY, isValidApiary);
        nbt.putInt(NBTConstants.NBT_VERT_OFFSET, getVerticalOffset());
        nbt.putInt(NBTConstants.NBT_HOR_OFFSET, getHorizontalOffset());
        nbt.putInt(NBTConstants.NBT_WIDTH, getWidth());
        nbt.putInt(NBTConstants.NBT_HEIGHT, getHeight());
        nbt.putInt(NBTConstants.NBT_DEPTH, getDepth());
        if (getStoragePos() != null)
            nbt.put(NBTConstants.NBT_STORAGE_POS, NBTUtil.writeBlockPos(getStoragePos()));
        if (getBreederPos() != null)
            nbt.put(NBTConstants.NBT_BREEDER_POS, NBTUtil.writeBlockPos(getBreederPos()));
        return nbt;
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        save(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public void handleUpdateTag(@NotNull BlockState state, CompoundNBT tag) {
        this.load(state, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        loadFromNBT(pkt.getTag());
    }
}
