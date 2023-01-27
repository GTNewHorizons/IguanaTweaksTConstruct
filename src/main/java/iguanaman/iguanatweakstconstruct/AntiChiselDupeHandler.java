package iguanaman.iguanatweakstconstruct;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.item.ItemStack;
import tconstruct.tools.TinkerTools;

public class AntiChiselDupeHandler {
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting == null || event.crafting.getItem() != TinkerTools.chisel) return;

        // the output was a chisel, we therefore either replaced something or fixed it or repaired it or whatever
        // we remove the chisel form the crafting grid otherwise it's duped
        for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
            ItemStack stack = event.craftMatrix.getStackInSlot(i);
            if (stack != null && stack.getItem() == TinkerTools.chisel)
                event.craftMatrix.setInventorySlotContents(i, null);
        }
    }
}
