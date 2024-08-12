//package org.dawnoftimebuilder.registry;
//
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.MenuType;
//import org.dawnoftimebuilder.container.DisplayerMenu;
//
//import java.util.function.Supplier;
//
//import static org.dawnoftimebuilder.DawnOfTimeBuilder.MOD_ID;
// TODO: Menu Types
//public class DoTBMenuTypesRegistry {
//	public static DoTBMenuTypesRegistry INSTANCE;
////	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
//
//	public final Supplier<MenuType<DisplayerMenu>> DISPLAYER = register("displayer", () -> IForgeMenuType.create(DisplayerMenu::new));
//
//	public abstract <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(String name, Supplier<MenuType<T>> menuType);
//
////	private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> reg(String name, Supplier<MenuType<T>> menuType) {
////		return MENU_TYPES.register(name, menuType);
////	}
//}
