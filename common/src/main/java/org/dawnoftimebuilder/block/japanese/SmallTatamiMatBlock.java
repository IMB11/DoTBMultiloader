package org.dawnoftimebuilder.block.japanese;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.dawnoftimebuilder.block.IBlockChain;
import org.dawnoftimebuilder.block.templates.WaterloggedBlock;
import org.dawnoftimebuilder.registry.DoTBBlocksRegistry;
import org.dawnoftimebuilder.registry.DoTBTags;
import org.dawnoftimebuilder.util.BlockStatePropertiesAA;
import org.dawnoftimebuilder.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.world.level.block.Blocks.SPRUCE_PLANKS;
import static org.dawnoftimebuilder.util.VoxelShapes.SMALL_TATAMI_MAT_SHAPES;

public class SmallTatamiMatBlock extends WaterloggedBlock implements IBlockChain {
    public static final EnumProperty<Direction.Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty ROLLED = BlockStatePropertiesAA.ROLLED;
    public static final IntegerProperty STACK = BlockStatePropertiesAA.STACK;

    public SmallTatamiMatBlock(Properties properties) {
        super(properties, SMALL_TATAMI_MAT_SHAPES);
        this.registerDefaultState(this.defaultBlockState().setValue(ROLLED, false).setValue(ATTACHED, false).setValue(STACK, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ATTACHED, HORIZONTAL_AXIS, ROLLED, STACK);
    }

    @Override
    public int getShapeIndex(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if(state.getValue(ATTACHED))
            return 7;
        if(state.getValue(ROLLED)) {
            boolean isAxisX = state.getValue(HORIZONTAL_AXIS) == Direction.Axis.X;
            return switch (state.getValue(STACK)) {
                default -> isAxisX ? 1 : 4;
                case 2 -> isAxisX ? 2 : 5;
                case 3 -> isAxisX ? 3 : 6;
            };
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState oldState = world.getBlockState(pos);
        if(oldState.getBlock() == this) {
            int stack = oldState.getValue(STACK);
            if(oldState.getValue(ROLLED) && !oldState.getValue(ATTACHED) && stack < 3) {
                return oldState.setValue(STACK, stack + 1);
            }
        }
        return super.getStateForPlacement(context)
                .setValue(ROLLED, world.getBlockState(pos.below()).is(DoTBTags.INSTANCE.COVERED_BLOCKS))
                .setValue(HORIZONTAL_AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        if(state.getValue(ATTACHED)) {
            BlockState stateUp = worldIn.getBlockState(pos.above());
            Block blockUp = stateUp.getBlock();
            if(stateUp.is(BlockTags.FENCES) || worldIn.getBlockState(pos.below()).is(BlockTags.FENCES))
                return true;
            if(IBlockChain.canBeChained(stateUp, true))
                return true;
        }
        return !worldIn.isEmptyBlock(pos.below());
    }

    @Override
    public @NotNull BlockState updateShape(BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor worldIn, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        stateIn = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if(facing.getAxis().isVertical()) {
            stateIn = stateIn.setValue(ATTACHED, false);
            if(stateIn.getValue(ROLLED) && stateIn.getValue(STACK) == 1) {
                BlockState stateUp = worldIn.getBlockState(currentPos.above());
                if(stateUp.is(BlockTags.FENCES)
                        || worldIn.getBlockState(currentPos.below()).is(BlockTags.FENCES)
                        || IBlockChain.canBeChained(stateUp, true))
                    stateIn = stateIn.setValue(ATTACHED, true);
            }
            return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : this.tryMergingWithSprucePlanks(stateIn, worldIn, currentPos);
        }
        return stateIn;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockState newState = this.tryMergingWithSprucePlanks(state, worldIn, pos);
        if(newState.getBlock() == Blocks.AIR)
            worldIn.setBlock(pos, newState, 10);
    }

    private BlockState tryMergingWithSprucePlanks(BlockState state, LevelAccessor worldIn, BlockPos pos) {
        if(state.getValue(ROLLED))
            return state;
        Block blockDown = worldIn.getBlockState(pos.below()).getBlock();
        if(blockDown == SPRUCE_PLANKS) {
            worldIn.setBlock(pos.below(), DoTBBlocksRegistry.INSTANCE.SMALL_TATAMI_FLOOR.get().defaultBlockState(), 10);
            return Blocks.AIR.defaultBlockState();
        }
        return state.setValue(ATTACHED, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if(player.isCrouching()) {
            int stack = state.getValue(STACK);
            boolean isRolled = state.getValue(ROLLED);
            if(isRolled && stack == 1)
                if(worldIn.getBlockState(pos.below()).is(DoTBTags.INSTANCE.COVERED_BLOCKS))
                    return InteractionResult.PASS;
            if(state.getValue(STACK) > 1) {
                state = state.setValue(STACK, stack - 1);
                Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this.asItem()));
            } else
                state = state.setValue(ROLLED, !isRolled);
            state = this.updateShape(state, Direction.DOWN, worldIn.getBlockState(pos.below()), worldIn, pos, pos.below());
            worldIn.setBlock(pos, state, 10);
            worldIn.playSound(player, pos, this.soundType.getPlaceSound(), SoundSource.BLOCKS, (this.soundType.getVolume() + 1.0F) / 2.0F, this.soundType.getPitch() * 0.8F);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        ItemStack itemstack = useContext.getItemInHand();
        if(itemstack.getItem() == this.asItem()) {
            if(!state.getValue(ROLLED) || state.getValue(ATTACHED) || state.getValue(STACK) == 3)
                return false;
            return useContext.replacingClickedOnBlock();
        }
        return false;
    }

    @Override
    public boolean canConnectToChainAbove(BlockState state) {
        return state.getValue(ATTACHED);
    }

    @Override
    public boolean canConnectToChainUnder(BlockState state) {
        return state.getValue(ATTACHED);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(HORIZONTAL_AXIS, state.getValue(HORIZONTAL_AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Utils.addTooltip(tooltip, this);
    }
}