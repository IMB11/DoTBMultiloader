package org.dawnoftimebuilder.block.templates;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.dawnoftimebuilder.block.IBlockClimbingPlant;
import org.dawnoftimebuilder.util.BlockStatePropertiesAA;
import org.dawnoftimebuilder.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static net.minecraft.tags.BlockTags.DIRT;
import static org.dawnoftimebuilder.util.Utils.TOOLTIP_CLIMBING_PLANT;
import static org.dawnoftimebuilder.util.VoxelShapes.LATTICE_SHAPES;

public class LatticeBlock extends WaterloggedBlock implements IBlockClimbingPlant {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final EnumProperty<BlockStatePropertiesAA.ClimbingPlant> CLIMBING_PLANT = BlockStatePropertiesAA.CLIMBING_PLANT;
    private static final IntegerProperty AGE = BlockStatePropertiesAA.AGE_0_6;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;

    public LatticeBlock(Properties properties) {
        super(properties, LATTICE_SHAPES);
        this.registerDefaultState(this.defaultBlockState().setValue(CLIMBING_PLANT, BlockStatePropertiesAA.ClimbingPlant.NONE).setValue(AGE, 0).setValue(WATERLOGGED, false).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(PERSISTENT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, CLIMBING_PLANT, AGE, PERSISTENT);
    }

    @Override
    public int getShapeIndex(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        int index = 0;
        if(state.getValue(SOUTH))
            index += 1;
        if(state.getValue(WEST))
            index += 2;
        if(state.getValue(NORTH))
            index += 4;
        if(state.getValue(EAST))
            index += 8;
        if(index > 14)
            index = 0;
        return index;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if(state.getBlock() != this)
            state = super.getStateForPlacement(context);
        return switch (context.getHorizontalDirection()) {
            default -> state.setValue(SOUTH, true);
            case WEST -> state.setValue(WEST, true);
            case NORTH -> state.setValue(NORTH, true);
            case EAST -> state.setValue(EAST, true);
        };
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext useContext) {
        ItemStack itemstack = useContext.getItemInHand();
        if(useContext.getPlayer() != null && useContext.getPlayer().isCrouching())
            return false;
        if(itemstack.getItem() == this.asItem()) {
            Direction newDirection = useContext.getHorizontalDirection();
            return switch (newDirection) {
                default -> !state.getValue(SOUTH);
                case WEST -> !state.getValue(WEST);
                case NORTH -> !state.getValue(NORTH);
                case EAST -> !state.getValue(EAST);
            };
        }
        return false;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(CLIMBING_PLANT).hasNoPlant();
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel worldIn, BlockPos pos, ItemStack stack, boolean p_222953_) {
        super.spawnAfterBreak(state, worldIn, pos, stack, p_222953_);
        //Be careful, climbing plants are not dropping from block's loot_table, but from their own loot_table
        this.dropPlant(state, worldIn, pos, stack, p_222953_);
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        this.tickPlant(state, worldIn, pos, random);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if(!state.getValue(PERSISTENT)) {
            if(Utils.useLighter(worldIn, pos, player, handIn)) {
                Random rand = new Random();
                for(int i = 0; i < 5; i++) {
                    worldIn.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + rand.nextDouble(), (double) pos.getY() + 0.5D + rand.nextDouble() / 2, (double) pos.getZ() + rand.nextDouble(), 0.0D, 0.07D, 0.0D);
                }
                worldIn.setBlock(pos, state.setValue(PERSISTENT, true), 10);
                return InteractionResult.SUCCESS;
            }
        }
        if(player.isCreative()) {
            if(this.tryPlacingPlant(state, worldIn, pos, player, handIn))
                return InteractionResult.SUCCESS;
        } else {
            if(worldIn.getBlockState(pos.below()).is(DIRT)) {
                if(this.tryPlacingPlant(state, worldIn, pos, player, handIn))
                    return InteractionResult.SUCCESS;
            }
        }
        return this.harvestPlant(state, worldIn, pos, player, handIn);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Utils.addTooltip(tooltip, TOOLTIP_CLIMBING_PLANT);
    }
}
