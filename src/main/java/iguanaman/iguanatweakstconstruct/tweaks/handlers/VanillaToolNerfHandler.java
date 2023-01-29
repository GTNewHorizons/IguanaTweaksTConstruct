package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.tweaks.IguanaTweaks;

public class VanillaToolNerfHandler {

    @SubscribeEvent
    public void breakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.entityPlayer == null) return;

        ItemStack itemStack = event.entityPlayer.getCurrentEquippedItem();
        if (itemStack == null) return;

        if (isUselessTool(itemStack.getItem())) event.newSpeed = 0;
    }

    @SubscribeEvent
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;

        if (isUselessTool(event.itemStack.getItem())) {
            event.toolTip.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.uselessTool1"));
            event.toolTip.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.uselessTool2"));
        }
    }

    public static boolean isUselessTool(Item item) {
        if (item == null) return false;

        if (IguanaTweaks.toolWhitelist.contains(item)) return false;

        if (item instanceof ItemTool) return true;

        return false;
    }
}
