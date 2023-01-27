package iguanaman.iguanatweakstconstruct.mobheads.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.mobheads.renderers.IguanaTileEntitySkullRenderer;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderPlayerHandler {
    @SubscribeEvent
    public void renderSkullHelmet(RenderPlayerEvent.SetArmorModel event) {
        ItemStack itemStack = event.entityPlayer.inventory.armorItemInSlot(3);
        if (itemStack == null) return;

        Item item = itemStack.getItem();
        boolean isBucket =
                IguanaTweaksTConstruct.pulsar.isPulseLoaded(Reference.PULSE_ITEMS) && item == IguanaMobHeads.wearables;
        if (item != IguanaMobHeads.skullItem && !isBucket) return;

        GL11.glPushMatrix();
        event.renderer.modelBipedMain.bipedHead.postRender(0.0625F);
        float f1 = 1.0625F;
        GL11.glScalef(f1, -f1, -f1);
        if (isBucket)
            IguanaTileEntitySkullRenderer.renderer.renderBucket(
                    -0.5f, 0.0f, -0.5f, 180.0f, 1, itemStack.getItemDamage());
        else
            IguanaTileEntitySkullRenderer.renderer.renderSkull(
                    -0.5f, 0.0f, -0.5f, 180.0f, 1, itemStack.getItemDamage());
        // TileEntitySkullRenderer.field_147536_b.func_152674_a(-0.5F, 0.0F, -0.5F, 1, 00.0F, 0, null);
        GL11.glPopMatrix();
    }
}
