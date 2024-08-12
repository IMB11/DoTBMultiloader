package org.dawnoftimebuilder.block.general;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.dawnoftimebuilder.block.templates.PlateBlock;
import org.dawnoftimebuilder.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static org.dawnoftimebuilder.util.VoxelShapes.IRON_FENCE_SHAPES;

public class IronFenceBlock extends PlateBlock {
    private static final BooleanProperty UP = BlockStateProperties.UP;

    public IronFenceBlock(Properties properties) {
        super(properties, IRON_FENCE_SHAPES);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(PlateBlock.FACING, Direction.NORTH)
                .setValue(PlateBlock.SHAPE, StairsShape.STRAIGHT)
                .setValue(UP, true)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public int getShapeIndex(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        int index = (state.getValue(FACING).get2DDataValue() + 2) % 4;
        index *= 3;
        switch (state.getValue(SHAPE)) {
            default -> {}
            case OUTER_RIGHT -> index += 3;
            case STRAIGHT -> index += 1;
            case INNER_LEFT -> index += 2;
            case INNER_RIGHT -> index += 5;
        }
        index %= 12;
        return state.getValue(UP) ? index + 12 : index;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UP);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(UP, !context.getLevel().getBlockState(context.getClickedPos().above()).is(this));
    }

    @Override
    public @NotNull BlockState updateShape(BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor worldIn, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if(facing == Direction.UP) {
            stateIn = stateIn.setValue(UP, !facingState.is(this));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos, final Player player, final InteractionHand handIn, final BlockHitResult hit) {
        final ItemStack heldItemStack = player.getItemInHand(handIn);
        if(player.isCrouching()) {
            //We remove the highest Block
            if(state.getValue(UP)) {
                return super.use(state, worldIn, pos, player, handIn, hit);
            }
            final BlockPos topPos = this.getHighestColumnPos(worldIn, pos);
            if(topPos != pos) {
                if(!worldIn.isClientSide()) {
                    worldIn.setBlock(topPos, Blocks.AIR.defaultBlockState(), 35);
                    if(!player.isCreative()) {
                        Block.dropResources(state, worldIn, pos, null, player, heldItemStack);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        } else if(!heldItemStack.isEmpty() && heldItemStack.getItem() == this.asItem()) {
            //We put a ColumnBlock on top of the column
            final BlockPos topPos = this.getHighestColumnPos(worldIn, pos).above();
            if(topPos.getY() <= Utils.HIGHEST_Y) {
                if(!worldIn.isClientSide() && worldIn.getBlockState(topPos).isAir()) {
                    worldIn.setBlock(topPos, state, 11);
                    if(!player.isCreative()) {
                        heldItemStack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, worldIn, pos, player, handIn, hit);
    }

    private BlockPos getHighestColumnPos(final Level worldIn, final BlockPos pos) {
        int yOffset;
        for(yOffset = 0; yOffset + pos.getY() <= Utils.HIGHEST_Y; yOffset++) {
            if(worldIn.getBlockState(pos.above(yOffset)).getBlock() != this) {
                break;
            }
        }
        return pos.above(yOffset - 1);
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final BlockGetter worldIn, final List<Component> tooltip, final TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Utils.addTooltip(tooltip, Utils.TOOLTIP_COLUMN);
    }
}
