package iguanaman.iguanatweakstconstruct.modcompat.fmp;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import codechicken.microblock.Saw;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;

public class SawStrengthHandler {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!(event.itemStack.getItem() instanceof Saw)) return;

        event.toolTip.add(
                String.format(
                        "%sCutting Strength: %s",
                        EnumChatFormatting.GOLD,
                        HarvestLevels.getHarvestLevelName(
                                ((Saw) event.itemStack.getItem()).getCuttingStrength(event.itemStack))));
    }
}
