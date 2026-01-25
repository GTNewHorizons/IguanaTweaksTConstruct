package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.event.ToolCraftEvent;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.IToolPart;
import tconstruct.weaponry.TinkerWeaponry;
import tconstruct.weaponry.ammo.ArrowAmmo;
import tconstruct.weaponry.ammo.BoltAmmo;
import tconstruct.weaponry.weapons.Crossbow;
import tconstruct.weaponry.weapons.LongBow;
import tconstruct.weaponry.weapons.ShortBow;

public class StoneToolHandler {

    // we can initialize this statically, because it wont be initialized until PostInit, where all materials are already
    // registered
    private static ToolMaterial stoneMaterial = TConstructRegistry.getMaterial("Stone");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;

        // we're only interested if it's a tool part
        if (!(event.itemStack.getItem() instanceof IToolPart) || event.itemStack.getItem() == TinkerWeaponry.bowstring
                || event.itemStack.getItem() == TinkerWeaponry.fletching
                || event.itemStack.getItem() == TinkerWeaponry.partArrowShaft)
            return;

        ItemStack stack = event.itemStack;
        IToolPart part = (IToolPart) stack.getItem();

        // stone parts disabled?
        if (TConstructRegistry.getMaterial(part.getMaterialID(stack)) == stoneMaterial) {
            event.toolTip.add(1, "");
            event.toolTip
                    .add(2, EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.part.castonly1"));
            event.toolTip
                    .add(3, EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.part.castonly2"));
            // we abuse the fact that the result is not used by anything to signal our other handlers to not add another
            // tooltip
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onToolCraft(ToolCraftEvent event) {
        for (int i = 0; i < event.materials.length; i++) {
            // ignore bowstring and fletchings
            if (event.tool instanceof ShortBow && i == 1) continue;
            if (event.tool instanceof LongBow && i == 1) continue;
            if (event.tool instanceof Crossbow && i == 2) continue;
            if (event.tool instanceof ArrowAmmo && i >= 1) continue;
            if (event.tool instanceof BoltAmmo && i == 2) continue;

            // don't allow stone tools
            if (event.materials[i] == stoneMaterial) event.setResult(Event.Result.DENY);
        }
    }
}
