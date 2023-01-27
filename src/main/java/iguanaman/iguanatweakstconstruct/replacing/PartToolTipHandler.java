package iguanaman.iguanatweakstconstruct.replacing;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.IToolPart;

public class PartToolTipHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;

        // we're only interested if it's a tool part
        if (!(event.itemStack.getItem() instanceof IToolPart)) return;

        // we abuse the fact that the result is not used by anything else.
        // Some other tooltip handlers already added a different tooltip, so this part is not replaceable
        if (event.getResult() == Event.Result.DENY) return;

        ItemStack stack = event.itemStack;
        IToolPart part = (IToolPart) stack.getItem();

        ToolMaterial mat = TConstructRegistry.getMaterial(part.getMaterialID(stack));
        if (mat == null) return;

        String ability = mat.ability();
        if (ability == null) return;
        // paper or thaumium?
        if (ability.equals(StatCollector.translateToLocal("materialtraits.writable"))
                || ability.equals(StatCollector.translateToLocal("materialtraits.thaumic"))) {
            event.toolTip.add("");
            event.toolTip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("tooltip.part.needsmodifier1"));
            event.toolTip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("tooltip.part.needsmodifier2"));
        } else {
            event.toolTip.add("");
            event.toolTip.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("tooltip.part.replaceable"));
        }
    }
}
