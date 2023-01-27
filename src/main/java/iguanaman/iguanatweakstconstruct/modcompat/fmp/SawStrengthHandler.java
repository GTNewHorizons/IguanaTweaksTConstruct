package iguanaman.iguanatweakstconstruct.modcompat.fmp;

import codechicken.microblock.Saw;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class SawStrengthHandler {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!(event.itemStack.getItem() instanceof Saw)) return;

        event.toolTip.add(String.format(
                "%sCutting Strength: %s",
                ChatFormatting.GOLD.toString(),
                HarvestLevels.getHarvestLevelName(
                        ((Saw) event.itemStack.getItem()).getCuttingStrength(event.itemStack))));
    }
}
