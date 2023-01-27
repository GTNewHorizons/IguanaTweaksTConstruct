package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.tweaks.IguanaTweaks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;

public class VanillaHoeNerfHandler {
    @SubscribeEvent
    public void onHoeBlock(UseHoeEvent event) {
        // don't modify hoeing without tool (from machines, if they even send an event.)
        if (event.current == null) return;

        if (isUselessHoe(event.current.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;

        if (isUselessHoe(event.itemStack.getItem())) {
            event.toolTip.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.uselessHoe1"));
            event.toolTip.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.uselessTool2"));
        }
    }

    public static boolean isUselessHoe(Item item) {
        if (item == null) return false;

        if (IguanaTweaks.toolWhitelist.contains(item)) return false;

        if (item instanceof ItemHoe) return true;

        return false;
    }
}
