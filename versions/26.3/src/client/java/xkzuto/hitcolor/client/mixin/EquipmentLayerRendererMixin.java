package xkzuto.hitcolor.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xkzuto.hitcolor.client.config.HitColorConfig;

@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerRendererMixin {

    @Unique
    private static final ThreadLocal<Object> extendedhitcolor$currentState = new ThreadLocal<>();

    @Unique
    private static boolean extendedhitcolor$shouldTint() {
        Object state = extendedhitcolor$currentState.get();
        return HitColorConfig.get().armorHitColorEnabled
                && state instanceof LivingEntityRenderState s
                && s.hasRedOverlay;
    }

    @Inject(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At("HEAD")
    )
    private void onRenderLayersHead(EquipmentClientInfo.LayerType layerType, ResourceKey<?> equipmentAssetId, Model<?> model, Object state, ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, Identifier playerTextureOverride, int outlineColor, int order, CallbackInfo ci) {
        extendedhitcolor$currentState.set(state);
    }

    @Inject(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At("RETURN")
    )
    private void onRenderLayersReturn(CallbackInfo ci) {
        extendedhitcolor$currentState.remove();
    }

    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType wrapArmorCutoutNoCull(Identifier texture, Operation<RenderType> original) {
        if (extendedhitcolor$shouldTint()) {
            return RenderTypes.entityCutout(texture);
        }
        return original.call(texture);
    }

    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCullGlint(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType wrapArmorCutoutNoCullGlint(Identifier texture, Operation<RenderType> original) {
        if (extendedhitcolor$shouldTint()) {
            return RenderTypes.entityCutout(texture);
        }
        return original.call(texture);
    }

    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorTrim(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType wrapArmorTrim(Identifier texture, boolean decal, Operation<RenderType> original) {
        if (extendedhitcolor$shouldTint()) {
            return RenderTypes.entityCutout(texture);
        }
        return original.call(texture, decal);
    }

    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"
            )
    )
    private <S> void wrapSubmitModel(
            OrderedSubmitNodeCollector collector,
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int overlay,
            int color,
            UvMapping uvMapping,
            int order,
            Operation<Void> original
    ) {
        int finalOverlay = overlay;
        if (extendedhitcolor$shouldTint()) {
            finalOverlay = OverlayTexture.pack(0, OverlayTexture.v(true));
        }
        original.call(collector, model, state, poseStack, renderType, packedLight, finalOverlay, color, uvMapping, order);
    }
}
