package org.valkyrienskies.mod.common;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.entity.EntityMountableChair;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

@Mod.EventBusSubscriber(modid = ValkyrienSkiesMod.MOD_ID)
public class RegisterEvents {

    private static final Logger logger = LogManager.getLogger(RegisterEvents.class);

    @SubscribeEvent
    public static void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
        logger.debug("Registering blocks");
        ValkyrienSkiesMod.INSTANCE.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(@Nonnull final RegistryEvent.Register<Item> event) {
        logger.debug("Registering items");
        ValkyrienSkiesMod.INSTANCE.registerItems(event);
    }

    @SubscribeEvent
    public static void registerRecipes(@Nonnull final RegistryEvent.Register<IRecipe> event) {
        ValkyrienSkiesMod.INSTANCE.registerRecipes(event);
    }

    /**
     * This method will be called by Forge when it is time for the mod to register its entity
     * entries.
     */
    @SubscribeEvent
    public static void onRegisterEntitiesEvent(
        @Nonnull final RegistryEvent.Register<EntityEntry> event) {
        final ResourceLocation physicsWrapperEntity = new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
            "PhysWrapper");
        final ResourceLocation entityMountable = new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
            "entity_mountable");
        final ResourceLocation entityMountableChair = new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
            "entity_mountable_chair");
        // Used to ensure no duplicates of entity network id's
        int entityId = 0;
        event.getRegistry()
            .registerAll(EntityEntryBuilder.create()
                    .entity(PhysicsWrapperEntity.class)
                    .id(physicsWrapperEntity, entityId++)
                    .name(physicsWrapperEntity.getPath())
                    .tracker(ValkyrienSkiesMod.VS_ENTITY_LOAD_DISTANCE, 1, false)
                    .build(),
                EntityEntryBuilder.create()
                    .entity(EntityMountable.class)
                    .id(entityMountable, entityId++)
                    .name(entityMountable.getPath())
                    .tracker(ValkyrienSkiesMod.VS_ENTITY_LOAD_DISTANCE, 1, false)
                    .build(),
                EntityEntryBuilder.create()
                    .entity(EntityMountableChair.class)
                    .id(entityMountableChair, entityId++)
                    .name(entityMountableChair.getPath())
                    .tracker(ValkyrienSkiesMod.VS_ENTITY_LOAD_DISTANCE, 1, false)
                    .build()
            );
    }
}
