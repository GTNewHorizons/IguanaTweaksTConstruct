package iguanaman.iguanatweakstconstruct.restriction;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import java.util.List;
import java.util.Map;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import tconstruct.library.TConstructRegistry;
import tconstruct.world.TinkerWorld;

@Pulse(
        id = Reference.PULSE_RESTRICTIONS,
        description = "Various Tweaks for vanilla Minecraft and Tinker's Construct. See Config.")
public class IguanaPartRestriction {
    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        // init the helper
        RestrictionHelper.init();

        RestrictionConfig config = new RestrictionConfig();
        config.init("restrictions.cfg");

        adaptChestLoot();

        MinecraftForge.EVENT_BUS.register(new PartRestrictionHandler());
    }

    // removes all restricted parts from tinkerer chests in villages n stuff
    private void adaptChestLoot() {
        // this would be so much easier if i could just iterate over the contents. but we'll have to copy a few times
        // luckily this is only executed once on startup :)
        for (Map.Entry<List, ItemStack> entry : TConstructRegistry.patternPartMapping.entrySet()) {
            Item pattern = (Item) entry.getKey().get(0); // the pattern
            Integer meta = (Integer) entry.getKey().get(1); // metadata of the pattern
            Integer matID = (Integer) entry.getKey().get(2); // Material-ID of the material needed to craft
            ItemStack part = (ItemStack) entry.getValue();

            // remove if restricted
            if (RestrictionHelper.isRestricted(new ItemStack(pattern, 1, meta), TConstructRegistry.getMaterial(matID)))
                TinkerWorld.tinkerHouseChest.removeItem(part);
        }
    }
}
