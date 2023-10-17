package iguanaman.iguanatweakstconstruct.modcompat.fmp;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.uv.UVTranslation;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.SwapYZ;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.Translation;

public class IguanaItemSawRenderer implements IItemRenderer {

    private static Map<String, CCModel> models = CCModel
            .parseObjModels(new ResourceLocation("microblock", "models/saw.obj"), 7, new SwapYZ());
    private static CCModel handle = models.get("Handle");
    private static CCModel holder = models.get("BladeSupport");
    private static CCModel blade = models.get("Blade");

    private final String texture;
    private final int uvIndex;

    public IguanaItemSawRenderer(String texture, int uvIndex) {
        this.texture = texture;
        this.uvIndex = uvIndex;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        TransformationList t;
        switch (type) {
            case INVENTORY:
                t = new TransformationList(
                        new Scale(1.8),
                        new Translation(0, 0, -0.6),
                        new Rotation(-Math.PI / 4d, 1, 0, 0),
                        new Rotation(Math.PI * 3d / 4, 0, 1, 0));
                break;
            case ENTITY:
                t = new TransformationList(
                        new Scale(1),
                        new Translation(0, 0, -0.25),
                        new Rotation(-Math.PI / 4d, 1, 0, 0));
                break;
            case EQUIPPED_FIRST_PERSON:
                t = new TransformationList(
                        new Scale(1.5),
                        new Rotation(-Math.PI / 3d, 1, 0, 0),
                        new Rotation(Math.PI * 3d / 4, 0, 1, 0),
                        new Translation(0.5, 0.5, 0.5));
                break;
            case EQUIPPED:
                t = new TransformationList(
                        new Scale(1.5),
                        new Rotation(-Math.PI / 5d, 1, 0, 0),
                        new Rotation(-Math.PI * 3d / 4, 0, 1, 0),
                        new Translation(0.75, 0.5, 0.75));
                break;
            default:
                return;
        }

        CCRenderState.reset();
        CCRenderState.useNormals = true;
        CCRenderState.pullLightmap();
        CCRenderState.changeTexture(texture);
        CCRenderState.startDrawing();
        handle.render(t);
        holder.render(t);
        CCRenderState.draw();
        GL11.glDisable(GL11.GL_CULL_FACE);
        CCRenderState.startDrawing();
        blade.render(t, new UVTranslation(0, uvIndex * 4 / 64D));
        CCRenderState.draw();
        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
