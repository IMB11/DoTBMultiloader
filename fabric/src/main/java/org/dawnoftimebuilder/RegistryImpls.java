package org.dawnoftimebuilder;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.dawnoftimebuilder.block.templates.FlowerPotBlockAA;
import org.dawnoftimebuilder.item.IHasFlowerPot;
import org.dawnoftimebuilder.registry.*;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegistryImpls {
    public static class FabricBlockEntitiesRegistry extends DoTBBlockEntitiesRegistry {
        @Override
        public <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String name, BiFunction<BlockPos, BlockState, T> factoryIn, Supplier<Block[]> validBlocksSupplier) {
            return () -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(Constants.MOD_ID, name), FabricBlockEntityTypeBuilder.create((FabricBlockEntityTypeBuilder.Factory<T>) factoryIn, validBlocksSupplier.get()).build());
        }
    }

    public static class FabricBlocksRegistry extends DoTBBlocksRegistry {
        @SafeVarargs
        @Override
        public final <T extends Block, Y extends Item> Supplier<T> registerWithItem(String id, Supplier<T> block, Function<T, Y> item, TagKey<Block>... tags) {
          Supplier<T> registryBlock = () -> Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(Constants.MOD_ID, id), block.get());
            if(item != null) {
                Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, id), item.apply(registryBlock.get()));
            }
            if(tags.length == 0){
                addBlockTag(registryBlock, BlockTags.MINEABLE_WITH_PICKAXE);
            }else{
                for (TagKey<Block> tag : tags) {
                    addBlockTag(registryBlock, tag);
                }
            }
            return registryBlock;
        }

        @Override
        public <T extends Block, Y extends Item & IHasFlowerPot> Supplier<T> registerWithFlowerPotItem(String blockID, Supplier<T> block, String itemID, Function<T, Y> item) {
            Supplier<T> toReturn = () -> Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(Constants.MOD_ID, blockID), block.get());
            if(item != null) {
                DoTBItemsRegistry.INSTANCE.registerWithFlowerPot(itemID, () -> item.apply(toReturn.get()));
            }
            // Flower can be broken with sword, and in the ItemRegistry, pot can be broken with Pickaxe.
            addBlockTag(toReturn, BlockTags.SWORD_EFFICIENT);
            return toReturn;
        }
    }

    public static class FabricEntitiesRegistry extends DoTBEntitiesRegistry {
        @Override
        public <T extends Entity> Supplier<EntityType<T>> register(String name, Supplier<EntityType.Builder<T>> builder) {
            return () -> Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(Constants.MOD_ID, name), builder.get().build(name));
        }
    }

    public static class FabricFeaturesRegistry extends DoTBFeaturesRegistry {
        @Override
        public <Y extends FeatureConfiguration, T extends Feature<Y>> Supplier<T> register(String name, Supplier<T> featureSupplier) {
            return () -> Registry.register(BuiltInRegistries.FEATURE, new ResourceLocation(Constants.MOD_ID, name), featureSupplier.get());
        }
    }

    public static class FabricItemsRegistry extends DoTBItemsRegistry {
        @Override
        public <T extends Item> Supplier<Item> register(String name, Supplier<T> itemSupplier) {
            return () -> Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, name), itemSupplier.get());
        }

        @Override
        public <T extends Item & IHasFlowerPot> Supplier<Item> registerWithFlowerPot(String name, Supplier<T> itemSupplier) {
            return registerWithFlowerPot(name, name, itemSupplier);
        }

        @Override
        public <T extends Item & IHasFlowerPot> Supplier<Item> registerWithFlowerPot(String plantName, String seedName, Supplier<T> itemSupplier) {
            final String potName = plantName + "_flower_pot";

            if(DoTBBlocksRegistry.INSTANCE == null) {
                DoTBBlocksRegistry.INSTANCE = new FabricBlocksRegistry();
            }

            Supplier<FlowerPotBlockAA> potBlockObject = DoTBBlocksRegistry.INSTANCE.register(potName, () -> {
                final FlowerPotBlockAA potBlock = new FlowerPotBlockAA(null);
                DoTBBlocksRegistry.POT_BLOCKS.put(potName, potBlock);
                return potBlock;
            }, BlockTags.MINEABLE_WITH_PICKAXE);

            return this.register(seedName, () -> {
                T item = itemSupplier.get();
                FlowerPotBlockAA potBlock = potBlockObject.get();

                item.setPotBlock(potBlock);
                potBlock.setItemInPot(item);

                return item;
            });
        }
    }

    public static class FabricMenuTypesRegistry extends DoTBMenuTypesRegistry {
        @Override
        public <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(String name, MenuTypeFactory<T> factory) {
            return () -> (MenuType<T>) Registry.register(BuiltInRegistries.MENU, new ResourceLocation(Constants.MOD_ID, name), new ExtendedScreenHandlerType<>(factory::create));
        }
    }

    public static class FabricRecipeSerializersRegistry extends DoTBRecipeSerializersRegistry {
        @Override
        public <T extends RecipeSerializer<? extends Recipe<?>>> Supplier<T> register(String name, Supplier<T> recipeSerializer) {
            return () -> Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(Constants.MOD_ID, name), recipeSerializer.get());
        }
    }

    public static class FabricRecipeTypesRegistry extends DoTBRecipeTypesRegistry {
        @Override
        public <T extends Recipe<?>> Supplier<RecipeType<T>> register(String name) {
            return () -> RecipeType.register(name);
        }
    }

    public static class FabricTagsRegistry extends DoTBTags {
        @Override
        public TagKey<Block> registerBlock(ResourceLocation id) {
            return TagKey.create(Registries.BLOCK, id);
        }

        @Override
        public TagKey<Item> registerItem(ResourceLocation id) {
            return TagKey.create(Registries.ITEM, id);
        }
    }

    public static void init() {
        DoTBItemsRegistry.INSTANCE = new FabricItemsRegistry();

        // May have already been initialized by the DoTBItems.registerWithFlowerPot method.
        if(DoTBBlocksRegistry.INSTANCE == null) {
            DoTBBlocksRegistry.INSTANCE = new FabricBlocksRegistry();
        }

        DoTBBlockEntitiesRegistry.INSTANCE = new FabricBlockEntitiesRegistry();
        DoTBEntitiesRegistry.INSTANCE = new FabricEntitiesRegistry();
        DoTBFeaturesRegistry.INSTANCE = new FabricFeaturesRegistry();
        DoTBMenuTypesRegistry.INSTANCE = new FabricMenuTypesRegistry();
        DoTBRecipeSerializersRegistry.INSTANCE = new FabricRecipeSerializersRegistry();
        DoTBRecipeTypesRegistry.INSTANCE = new FabricRecipeTypesRegistry();
        DoTBTags.INSTANCE = new FabricTagsRegistry();
    }
}
