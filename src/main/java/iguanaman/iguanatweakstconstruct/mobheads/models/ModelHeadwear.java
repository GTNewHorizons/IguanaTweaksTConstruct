package iguanaman.iguanatweakstconstruct.mobheads.models;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeletonHead;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHeadwear extends ModelSkeletonHead {

    public ModelHeadwear(int offsetX, int offsetY, int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        this.skeletonHead = new ModelRenderer(this, offsetX, offsetY);
        this.skeletonHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.4F);
        this.skeletonHead.setRotationPoint(0.0F, 0.0F, 0.0F);
    }
}
