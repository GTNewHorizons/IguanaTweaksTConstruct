package iguanaman.iguanatweakstconstruct.leveling.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.mobheads.items.IguanaSkull;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSkull;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/**
 * Displays the tooltips on mob heads for the mob-head modifier.
 */
public class MobHeadTooltipHandler {
    @SubscribeEvent
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;

        int mlvl = -1;
        int meta = event.itemStack.getItemDamage();
        // iguana mobheads
        if (event.itemStack.getItem() instanceof IguanaSkull) {
            switch (meta) {
                    // pigman zombie
                case 1:
                    mlvl = HarvestLevels._5_diamond;
                    break;
                    // blaze
                case 2:
                    mlvl = HarvestLevels._6_obsidian;
                    break;
                    // blizz
                case 3:
                    mlvl = HarvestLevels._6_obsidian;
                    break;
                    // enderman
                case 0:
                    mlvl = HarvestLevels._7_ardite;
                    break;
            }
        }
        // vanilla mobheads
        else if (event.itemStack.getItem() instanceof ItemSkull) {
            switch (meta) {
                    // zombie
                case 2:
                    mlvl = HarvestLevels._2_copper;
                    break;
                    // skelly
                case 0:
                    mlvl = HarvestLevels._3_iron;
                    break;
                    // creeper
                case 4:
                    mlvl = HarvestLevels._5_diamond;
                    break;
                    // witherskelly
                case 1:
                    mlvl = HarvestLevels._8_cobalt;
                    break;
            }
        }
        // netherstar
        else if (event.itemStack.getItem() == Items.nether_star) mlvl = HarvestLevels._9_manyullym;

        if (mlvl > 0) {
            // we reduce the mining level by 1 because they're applicable UP TO that level, not including it. :)
            mlvl--;
            event.toolTip.add(EnumChatFormatting.GOLD
                    + StatCollector.translateToLocalFormatted(
                            "tooltip.mobhead.level", HarvestLevels.getHarvestLevelName(mlvl)));
        }
    }
}
