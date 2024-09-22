package com.squoshi.sailor.mixin;

import com.squoshi.sailor.Sailor;
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
        Sailor.onFall(pLevel, pState, pPos, pEntity, pFallDistance);
    }
}
