package com.moulberry.autovanishplayers.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.autovanishplayers.AutoVanishPlayers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer extends EntityRenderer<LivingEntity> {

    @Unique
    private boolean forceInvisibleRenderType = false;
    @Unique
    private int invisibleAlpha = 0xFF;

    protected MixinLivingEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    public void render(LivingEntity livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        forceInvisibleRenderType = false;
        if (AutoVanishPlayers.isAutoVanishPlayersEnabled && livingEntity instanceof RemotePlayer) { // Enabled
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            double x = livingEntity.getX();
            double y = Math.max(livingEntity.getY(), Math.min(livingEntity.getY() + livingEntity.getBbHeight(), camera.getPosition().y));
            double z = livingEntity.getZ();
            double distanceSq = camera.getPosition().distanceToSqr(x, y, z);
            if (distanceSq < 1) {
                ci.cancel();
            } else if (distanceSq < 4*4) {
                forceInvisibleRenderType = true;
                invisibleAlpha = (int)(distanceSq/16 * (0xFF - 0x20) + 0x20);
            }
        }
    }

    @ModifyArg(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"), index = 4)
    public int render_renderToBuffer(int argb) {
        if (forceInvisibleRenderType) {
            int oldAlpha = (argb >> 24) & 0xFF;
            int newAlpha = Math.min(oldAlpha, invisibleAlpha);
            argb &= 0xFFFFFF;
            argb |= newAlpha << 24;
        }
        return argb;
    }

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    public void getRenderType(LivingEntity livingEntity, boolean visible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (visible && forceInvisibleRenderType) {
            cir.setReturnValue(RenderType.itemEntityTranslucentCull(this.getTextureLocation(livingEntity)));
        }
    }

}
