package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import java.util.ListIterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.leveling.handlers.LevelingToolTipHandler;
import iguanaman.iguanatweakstconstruct.tweaks.IguanaTweaks;

public class VanillaSwordNerfHandler {

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.source.damageType.equals("player"))) return;

        // only players
        if (!(event.source.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.source.getEntity();

        // the tool
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) return;

        if (isUselessWeapon(stack.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;

        if (isUselessWeapon(event.itemStack.getItem())) {
            // remove +dmg stuff
            ListIterator<String> iter = event.toolTip.listIterator();
            while (iter.hasNext()) {
                if (iter.next().startsWith(LevelingToolTipHandler.plusPrefix)) iter.remove();
            }
            event.toolTip.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.uselessWeapon1"));
            event.toolTip.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.uselessTool2"));
        }
    }

    public static boolean isUselessWeapon(Item item) {
        if (item == null) return false;

        if (IguanaTweaks.toolWhitelist.contains(item)) return false;

        if (item instanceof ItemSword) return true;

        return false;
    }
}
