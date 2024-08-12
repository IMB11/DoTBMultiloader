package org.dawnoftimebuilder.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.dawnoftimebuilder.DawnOfTimeBuilder;

public abstract class DoTBTags {
    public static DoTBTags INSTANCE;
    //Item tags
    public final TagKey<Item> LIGHTERS = register(new ResourceLocation(DawnOfTimeBuilder.MOD_ID, "lighters"));
    //Block tags
    public final TagKey<Block> COVERED_BLOCKS = register(new ResourceLocation(DawnOfTimeBuilder.MOD_ID, "covered_blocks"));
    public final TagKey<Block> GRAVEL = register(new ResourceLocation("c", "gravel"));


    public abstract <T> TagKey<T> register(ResourceLocation id);
}
