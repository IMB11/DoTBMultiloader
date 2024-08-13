package org.dawnoftimebuilder.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.dawnoftimebuilder.DoTBConfig;
import org.dawnoftimebuilder.recipe.DryerRecipe;
import org.dawnoftimebuilder.registry.DoTBBlockEntitiesRegistry;
import org.dawnoftimebuilder.registry.DoTBRecipeTypesRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

public class DryerBlockEntity extends BlockEntity {
    public final SimpleContainer itemHandler = new SimpleContainer(2);
    private final int[] remainingTicks = new int[2];
    private boolean isInOperation;

    public DryerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(DoTBBlockEntitiesRegistry.INSTANCE.DRYER.get(), pPos, pBlockState);
    }

    public void tick() {
        if (this.getLevel() != null && this.isInOperation) {
            int finish = 0;
            boolean success = false;
            for (int i = 0; i < this.remainingTicks.length; i++) {
                this.remainingTicks[i]--;

                if (this.remainingTicks[i] <= 0) {
                    this.remainingTicks[i] = 0;
                    // Item dried, we replace it with the recipe result, and clear the recipe cached.
                    final DryerRecipe recipe = this.getDryerRecipe(new SimpleContainer(this.itemHandler.getItem(i)));

                    if (recipe != null) {
                        this.itemHandler.setItem(i, recipe.getResultItem(this.getLevel().registryAccess()).copy());
                        success = true;
                    }
                    finish++;
                }
            }

            if (success) {
                this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
            }

            if (finish >= 2) {
                this.isInOperation = false;
            }
        }
    }

    public InteractionResult tryInsertItemStack(final ItemStack itemStack, final boolean simple, final Level worldIn, final BlockPos pos, final Player player) {

        //Try to put the itemStack in an empty dryer
        if (this.putItemStackInFreeSpace(itemStack, simple, player)) {
            return InteractionResult.SUCCESS;
        }
        //No empty dryer, let's see if we could replace a dried item with ours
        if (simple) {
            if (this.itemIsDried(0)) {
                this.dropItemIndex(0, worldIn, pos);
                this.putItemStackInIndex(0, itemStack, player);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        final int index = this.dropOneDriedItem(worldIn, pos);
        if (index < 0) {
            return InteractionResult.PASS;
        }
        this.putItemStackInIndex(index, itemStack, player);
        return InteractionResult.SUCCESS;
    }

    public InteractionResult dropOneItem(final Level worldIn, final BlockPos pos) {
        if (this.dropOneDriedItem(worldIn, pos) > -1) {
            return InteractionResult.SUCCESS;
        }
        if (!this.itemHandler.getItem(0).isEmpty()) {
            this.dropItemIndex(0, worldIn, pos);
            return InteractionResult.SUCCESS;
        }
        if (!this.itemHandler.getItem(1).isEmpty()) {
            this.dropItemIndex(1, worldIn, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public int dropOneDriedItem(final Level worldIn, final BlockPos pos) {
        if (this.itemIsDried(0)) {
            this.dropItemIndex(0, worldIn, pos);
            return 0;
        }
        if (this.itemIsDried(1)) {
            this.dropItemIndex(1, worldIn, pos);
            return 1;
        }
        return -1;
    }

    private boolean itemIsDried(final int index) {
        if (this.itemHandler.getItem(index).isEmpty()) {
            return false;
        }
        return this.remainingTicks[index] <= 0;
    }

    private boolean putItemStackInFreeSpace(final ItemStack itemStack, final boolean simple, final Player player) {

        if (this.itemHandler.getItem(0).isEmpty() && this.putItemStackInIndex(0, itemStack, player)) {
            this.isInOperation = true;
            return true;
        }
        if (!simple && this.itemHandler.getItem(1).isEmpty() && this.putItemStackInIndex(1, itemStack, player)) {
            this.isInOperation = true;
            return true;
        }
        return false;
    }

    @Nullable
    private DryerRecipe getDryerRecipe(final SimpleContainer ingredientInventory) {
        if (this.getLevel() != null && !this.getLevel().isClientSide) {
            return this.getLevel().getRecipeManager().getRecipeFor(DoTBRecipeTypesRegistry.INSTANCE.DRYING.get(), ingredientInventory, this.getLevel()).orElse(null);
        }
        return null;
    }

    private boolean putItemStackInIndex(final int index, final ItemStack itemStack, final Player player) {
        //Tries to put the itemStack in a dryer : first we check if there is a corresponding recipe, then we set the variables.
        if (this.getLevel() != null) {
            final SimpleContainer invInHand = new SimpleContainer(itemStack);
            final DryerRecipe recipe = this.getDryerRecipe(invInHand);
            if (recipe != null && recipe.matches(invInHand, this.getLevel())) {
                this.itemHandler.setItem(index, recipe.getIngredients().get(0).getItems()[0].copy());
                if (!player.isCreative()) {
                    itemStack.shrink(recipe.getIngredients().get(0).getItems()[0].getCount());
                }
                final float timeVariation = new Random().nextFloat() * 2.0F - 1.0F;
                final int range = timeVariation >= 0 ? DoTBConfig.get().dryingTimeVariation : 10000 / (100 + DoTBConfig.get().dryingTimeVariation);
                this.remainingTicks[index] = (int) (recipe.getDryingTime() * (100 + timeVariation * range) / 100);
                this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);

                return true;
            }
        }
        return false;
    }

    private void dropItemIndex(final int index, final Level worldIn, final BlockPos pos) {
        Block.popResource(worldIn, pos, this.itemHandler.removeItem(index, 64));
        this.remainingTicks[index] = 0;
        if (this.getLevel() != null) {
            final BlockState state = this.getLevel().getBlockState(pos);
            this.getLevel().sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("inv", itemHandler.createTag());
        return tag;
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        tag.put("inv", itemHandler.createTag());
        for (int index = 0; index < 2; index++) {
            tag.putInt("remainingTime" + index, this.remainingTicks[index]);
        }
        tag.putBoolean("isInOperation", this.isInOperation);

        super.saveAdditional(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        itemHandler.fromTag(tag.getList("inv", CompoundTag.TAG_LIST));
        for (int index = 0; index < 2; index++) {
            this.remainingTicks[index] = tag.getInt("remainingTime" + index);
        }
        this.isInOperation = tag.getBoolean("isInOperation");

        super.load(tag);
    }
}