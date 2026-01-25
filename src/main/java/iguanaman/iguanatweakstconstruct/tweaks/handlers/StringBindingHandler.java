package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.event.ToolBuildEvent;
import tconstruct.library.event.ToolCraftEvent;
import tconstruct.tools.TinkerTools;

public class StringBindingHandler {

    @SubscribeEvent
    public void onToolBuild(ToolBuildEvent event) {
        if (event.accessoryStack == null || event.accessoryStack.getItem() == null) return;

        if (event.accessoryStack.getItem() == Items.string)
            event.accessoryStack = new ItemStack(TinkerTools.binding, 1, 40); // string binding
    }

    @SubscribeEvent
    public void onToolCraft(ToolCraftEvent event) {
        // we don't allow any string tools.
        // This basically is to hide the string material from players, since there are no craftable string parts
        if (event.materials[0] == TConstructRegistry.getMaterial(40)) event.setResult(Event.Result.DENY);
    }
}
