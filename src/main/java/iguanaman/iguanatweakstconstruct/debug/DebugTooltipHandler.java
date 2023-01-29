package iguanaman.iguanatweakstconstruct.debug;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;

public class DebugTooltipHandler {

    @SubscribeEvent
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null) return;
        if (event.itemStack == null) return;

        Block block = Block.getBlockFromItem(event.itemStack.getItem());
        if (block == null || block == Blocks.air) return;
        int meta = event.itemStack.getItemDamage();
        if (meta > 15) return;
        String toolclass = block.getHarvestTool(meta);

        int hlvl = block.getHarvestLevel(meta);
        if (hlvl >= 0) event.toolTip.add(
                EnumChatFormatting.GOLD
                        + String.format("Mineable with: %s %s", HarvestLevels.getHarvestLevelName(hlvl), toolclass));
        else event.toolTip.add(EnumChatFormatting.GOLD + "Mineable with: Unknown");
    }
}
