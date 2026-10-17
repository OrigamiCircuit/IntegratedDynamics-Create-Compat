package com.origamicircuit.integrateddynamicscreatecompat;

import com.simibubi.create.api.contraption.transformable.MovedBlockTransformerRegistries;
import com.simibubi.create.content.contraptions.StructureTransform;
import java.util.EnumMap;
import java.util.Map;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class CableRotationHandler {
    private CableRotationHandler() {}
    public static void register(final FMLCommonSetupEvent event) {
        MovedBlockTransformerRegistries.BLOCK_ENTITY_TRANSFORMERS.register(RegistryEntries.BLOCK_ENTITY_MULTIPART_TICKING, CableRotationHandler::transformCable);
    }
    private static void transformCable(BlockEntity blockEntity, StructureTransform transform) {
        if (!(blockEntity instanceof BlockEntityMultipartTicking multipart)) return;


System.out.println("Block state: " + blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()));


        Map<Direction, PartHelpers.PartStateHolder<?, ?>> originalParts = multipart.getPartContainer().getPartData();
        Map<Direction, PartHelpers.PartStateHolder<?, ?>> finalParts = new EnumMap<>(Direction.class);
System.out.println("Attempted to be rotated");
        for (Map.Entry<Direction, PartHelpers.PartStateHolder<?, ?>> entry : originalParts.entrySet()) {
            PartHelpers.PartStateHolder<?, ?> part = entry.getValue();
            Direction direction = entry.getKey();
            Direction mirrored = transform.mirrorFacing(direction);
            Direction rotated = transform.rotateFacing(mirrored);

            finalParts.put(rotated, part);
System.out.println(direction + " -> mirror: " + mirrored + " -> rotate: " + rotated);
        }
        multipart.getPartContainer().setPartData(finalParts);
        multipart.setChanged();
    }
}
