package iguanaman.iguanatweakstconstruct.override;

import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import tconstruct.util.IMCHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.Log;

/**
 * Allows to override: - All material properties - Mining Levels of all tools - Harvest Levels of all oredict Entries -
 * Harvest Levels of all blocks (overrides oredict)
 */
@Pulse(
        id = Reference.PULSE_OVERRIDE,
        description = "This module allows to override about any values relevant for TConstruct or IguanaTinkerTweaks.",
        defaultEnable = false)
public class IguanaOverride {

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        new ForgeHooks();
        doOverride("Material", new MaterialOverride());
        doOverride("Tool", new ToolOverride());
        doOverride("Block", new BlockOverride());
        doOverride("BonusModifier", new ModifierOverride());
        doOverride("HarvestLevelNames", new HarvestLevelNameOverride());

        MinecraftForge.EVENT_BUS.register(new ExtraHarvestLevelHandler());
    }

    public static void doOverride(String type, IOverride overrider) {
        String configFileName = type + "Override.cfg";
        String defaultFileName = type + "Defaults.cfg";

        try {
            Configuration defaultConfig = new Configuration(Reference.configFile(defaultFileName));
            overrider.createDefault(defaultConfig);
            defaultConfig.save();
        } catch (RuntimeException e) {
            IMCHandler.bigWarning("An Error occurred while creating default files for the %s Override", type);
            Log.error(e);
        }

        try {
            Configuration config = new Configuration(Reference.configFile(configFileName));
            config.load();
            overrider.processConfig(config);

            if (config.hasChanged()) config.save();
        } catch (RuntimeException e) {
            IMCHandler.bigWarning("An Error occurred while processing the Override for %s", type);
            Log.error(e);
        }
    }
}
