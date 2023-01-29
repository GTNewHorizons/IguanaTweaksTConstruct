package iguanaman.iguanatweakstconstruct.replacing;

import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;

import net.minecraftforge.common.MinecraftForge;

import tconstruct.library.crafting.ModifyBuilder;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import iguanaman.iguanatweakstconstruct.reference.Reference;

/**
 * Allows the replacement of toolparts. Basically it adds a modifier that turns each toolpart into a modifier.
 */

@Pulse(id = Reference.PULSE_REPLACING, description = "Replace parts of tools")
public class IguanaToolPartReplacing {

    @Handler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Handler
    public void init(FMLInitializationEvent event) {
        ModifyBuilder.registerModifier(new ModPartReplacement());
    }

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new PartToolTipHandler());
    }
}
