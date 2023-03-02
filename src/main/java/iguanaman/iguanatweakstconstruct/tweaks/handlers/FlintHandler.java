package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import java.util.ListIterator;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FlintHandler {

    @SubscribeEvent
    public void onBlockHarvested(BlockEvent.HarvestDropsEvent event) {
        // remove flint drop
        if (event.block != null && event.block == Blocks.gravel) {
            ListIterator<ItemStack> iter = event.drops.listIterator();
            boolean removedFlint = false;
            while (iter.hasNext()) {
                Item item = iter.next().getItem();
                if (item == Items.flint) {
                    iter.remove();
                    removedFlint = true;
                }
            }

            // ensure that gravel drops
            if (removedFlint) event.drops.add(new ItemStack(Blocks.gravel));
        }
    }
}
