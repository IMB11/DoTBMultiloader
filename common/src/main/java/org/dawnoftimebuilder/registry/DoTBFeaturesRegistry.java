package org.dawnoftimebuilder.registry;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import org.dawnoftimebuilder.DawnOfTimeBuilder;
import org.dawnoftimebuilder.worldgen.feature.DefaultCropsFeature;
import org.dawnoftimebuilder.worldgen.feature.DoTFeature;

import java.util.function.Supplier;

public abstract class DoTBFeaturesRegistry {
    public static DoTBFeaturesRegistry INSTANCE;
//    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, DawnOfTimeBuilder.MOD_ID);

    public final Supplier<Feature<SimpleBlockConfiguration>> DOT_FEATURE = register("dot_feature",
            () -> new DoTFeature(SimpleBlockConfiguration.CODEC));

    public final Supplier<Feature<RandomPatchConfiguration>> DEFAULT_CROPS = register("default_crops",
            () -> new DefaultCropsFeature(RandomPatchConfiguration.CODEC));

    public abstract <Y extends FeatureConfiguration, T extends Feature<Y>> Supplier<T> register(final String name, final Supplier<T> featureSupplier);
}
