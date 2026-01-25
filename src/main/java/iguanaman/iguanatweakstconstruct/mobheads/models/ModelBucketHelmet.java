package iguanaman.iguanatweakstconstruct.mobheads.models;

import net.minecraft.client.model.ModelRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBucketHelmet extends ModelEnderManHead {

    public ModelBucketHelmet() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.endermanHead = new ModelRenderer(this, 0, 0);
        this.endermanHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.endermanHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.endermanJaw = new ModelRenderer(this, 0, 16);
        this.endermanJaw.addBox(-4.0F, -5.5F, -2.0F, 8, 8, 8, 0.6F);
        this.endermanJaw.setRotationPoint(0.0F, 0.0F, 0.0F);
    }
}
