package com.origamicircuit.integrateddynamicscreatecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.origamicircuit.integrateddynamicscreatecompat.CreateSymmetryHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

@Mixin(ItemBlockCable.class)
public abstract class IntegratedDynamicsCableBlockItemMixin extends BlockItem{
    protected IntegratedDynamicsCableBlockItemMixin(Block block, Item.Properties props) {super(block, props);}
    
    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lorg/cyclops/integrateddynamics/item/ItemBlockCable;afterItemUse(Lnet/minecraft/world/item/context/UseOnContext;Lnet/minecraft/core/BlockPos;Lorg/cyclops/integrateddynamics/block/BlockCable;Z)V", ordinal = 0 ))
    private void onClickedPosition(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local ItemStack itemStack)
    {CreateSymmetryHelper.processSymmetry(context.getLevel(), context.getPlayer(), context.getHand(), itemStack, context.getClickedPos(), null, false);}
    
    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lorg/cyclops/integrateddynamics/item/ItemBlockCable;afterItemUse(Lnet/minecraft/world/item/context/UseOnContext;Lnet/minecraft/core/BlockPos;Lorg/cyclops/integrateddynamics/block/BlockCable;Z)V", ordinal = 1))
    private void onAdjacentPosition(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local ItemStack itemStack, @Local BlockPos posOffset)
    {CreateSymmetryHelper.processSymmetry(context.getLevel(), context.getPlayer(), context.getHand(), itemStack, posOffset, null, false);}
    
    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result.consumesAction())
            CreateSymmetryHelper.processSymmetry(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), context.getClickedPos(), null, false);
        return result;
    }
    
    @WrapWithCondition(method = "afterItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private boolean shouldShrinkStack(ItemStack stack, int amount, UseOnContext context) {return !context.getPlayer().isCreative();}
}
