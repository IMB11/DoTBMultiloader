//package org.dawnoftimebuilder.registry;
//
//import net.minecraft.core.registries.Registries;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.item.CreativeModeTab;
//import net.minecraft.world.item.ItemStack;
//import org.dawnoftimebuilder.DawnOfTimeBuilder;
//
//import java.util.function.Supplier;
// TODO: Creative Mode Tabs
//public abstract class DoTBCreativeModeTabsRegistry {
//    public static DoTBCreativeModeTabsRegistry INSTANCE;
////    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DawnOfTimeBuilder.MOD_ID);
//    public Supplier<CreativeModeTab> DOT_TAB = register("dot_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP).icon(() -> new ItemStack(DoTBItemsRegistry.INSTANCE.ANCIENTARCHI.get())).title(Component.translatable("itemGroup." + DawnOfTimeBuilder.MOD_ID + ".dottab")).build());
//
//    public abstract <T extends CreativeModeTab> Supplier<CreativeModeTab> register(final String name, final Supplier<T> tabSupplier);
//}
