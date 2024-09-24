package com.squoshi.sailor.mixin;

import com.squoshi.sailor.ServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "fallOn", at = @At("HEAD"))
    private void sailor$fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance, CallbackInfo ci) {
        if (pLevel.isClientSide()) return;
        ServerEvents.onFall(pLevel, pPos, pEntity, pFallDistance);
    }
    @Inject(method = "stepOn", at = @At("HEAD"))
    private void sailor$stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity, CallbackInfo ci) {
        if (pLevel.isClientSide()) return;
        ServerEvents.stepOn(pPos, pEntity);
    }
}
