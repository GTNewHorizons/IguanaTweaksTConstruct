package iguanaman.iguanatweakstconstruct.override;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

// This class handles harvest levels on the same block but where some metadata requires a tool,
// and some metadatas don't
public class ExtraHarvestLevelHandler {
    @SubscribeEvent
    public void breakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.entityPlayer == null) return;

        if (event.block == null || event.block == Blocks.air) return;

        Block block = event.block;
        int hlvl = event.block.getHarvestLevel(event.metadata);

        // does the block require a tool?
        if (hlvl <= 0)
            // no, nothing to do
            return;

        // tool requires a harvest level, but does the material require a tool?
        if (!block.getMaterial().isToolNotRequired())
            // a tool is required, we don't have to do anything
            return;

        // The tool does NOT require a tool, but has a harvest level for a tool set
        // we now manually check if this requiremnet is fulfilled

        String tool = event.block.getHarvestTool(event.metadata);
        ItemStack itemStack = event.entityPlayer.getCurrentEquippedItem();

        if (itemStack != null && itemStack.getItem() != null) {
            if (itemStack.getItem().getHarvestLevel(itemStack, tool) >= hlvl)
                // everything ok, correct tool
                return;
        }

        // we require a tool, but no fitting tool is present. we prevent any breaking
        event.setCanceled(true);
    }
}
