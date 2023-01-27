package iguanaman.iguanatweakstconstruct;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.claybuckets.IguanaItems;
import iguanaman.iguanatweakstconstruct.commands.CommandDumpOredict;
import iguanaman.iguanatweakstconstruct.commands.CommandDumpTools;
import iguanaman.iguanatweakstconstruct.commands.CommandIAmADirtyCheater;
import iguanaman.iguanatweakstconstruct.debug.DebugCommand;
import iguanaman.iguanatweakstconstruct.debug.IguanaDebug;
import iguanaman.iguanatweakstconstruct.harvestlevels.IguanaHarvestLevelTweaks;
import iguanaman.iguanatweakstconstruct.leveling.IguanaToolLeveling;
import iguanaman.iguanatweakstconstruct.leveling.commands.IguanaCommandLevelUpTool;
import iguanaman.iguanatweakstconstruct.leveling.commands.IguanaCommandToolXP;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.modcompat.fmp.IguanaFMPCompat;
import iguanaman.iguanatweakstconstruct.override.IguanaOverride;
import iguanaman.iguanatweakstconstruct.proxy.CommonProxy;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.replacing.IguanaToolPartReplacing;
import iguanaman.iguanatweakstconstruct.restriction.IguanaPartRestriction;
import iguanaman.iguanatweakstconstruct.tweaks.IguanaTweaks;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;
import iguanaman.iguanatweakstconstruct.worldgen.IguanaWorldGen;
import java.io.File;
import java.util.Random;
import mantle.pulsar.config.IConfiguration;
import mantle.pulsar.control.PulseManager;
import mantle.pulsar.pulse.PulseMeta;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = "GRADLETOKEN_VERSION",
        dependencies = "required-after:" + Reference.TCON_MOD_ID
                + "@[1.7.10-1.8.3,);after:ForgeMultipart@[1.1.1.321,);after:*")
public class IguanaTweaksTConstruct {

    // The instance of your mod that Forge uses.
    @Instance(Reference.MOD_ID)
    public static IguanaTweaksTConstruct instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = Reference.PROXY_CLIENT_CLASS, serverSide = Reference.PROXY_SERVER_CLASS)
    public static CommonProxy proxy;

    public static Random random = new Random();

    // use the PulseManager. This allows us to separate the different parts into independend modules and have stuff
    // together. yay.
    private IConfiguration pulseCFG;
    public static PulseManager pulsar;

    public static File configPath;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Log.init(event.getModLog());
        configPath = new File(event.getModConfigurationDirectory(), "IguanaTinkerTweaks");
        // update old config path
        File oldPath = new File(event.getModConfigurationDirectory(), Reference.MOD_ID);
        if (oldPath.exists()) oldPath.renameTo(configPath);

        configPath.mkdirs();

        // init pulse manager
        pulseCFG = new PulsarCFG(
                Reference.configFile("Modules.cfg"), "Tinker's Construct Addon: Iguana Tweaks for Tinkers Construct");
        pulseCFG.load();
        pulsar = new PulseManager(Reference.MOD_ID, pulseCFG);

        Config config = new Config();
        config.init(Reference.configFile("main.cfg"));
        // register config as eventhandler to get config changed updates
        FMLCommonHandler.instance().bus().register(config);

        // order matters here
        pulsar.registerPulse(new IguanaHarvestLevelTweaks());
        pulsar.registerPulse(new IguanaToolLeveling());
        pulsar.registerPulse(new IguanaMobHeads());
        pulsar.registerPulse(new IguanaItems());
        pulsar.registerPulse(new IguanaTweaks());
        // override before restriction because restrictions use ToolMaterial objects, and override can change them.
        pulsar.registerPulse(new IguanaOverride());
        pulsar.registerPulse(new IguanaPartRestriction());
        // replacing has to be after tweaks and restrictions, because its tooltips have to be handled last
        pulsar.registerPulse(new IguanaToolPartReplacing());
        pulsar.registerPulse(new IguanaWorldGen());
        pulsar.registerPulse(new IguanaDebug());

        // mod compat
        pulsar.registerPulse(new IguanaFMPCompat());

        // if we don't use our custom harvest levels, we have to adjust what we're using
        if (!pulsar.isPulseLoaded(Reference.PULSE_HARVESTTWEAKS)) HarvestLevels.adjustToVanillaLevels();
        // update harvest level strings
        HarvestLevels.updateHarvestLevelNames();

        // start up the pulses
        pulsar.preInit(event);

        // versionchecker support
        FMLInterModComms.sendRuntimeMessage(
                Reference.MOD_ID,
                "VersionChecker",
                "addVersionCheck",
                "https://raw.githubusercontent.com/SlimeKnights/IguanaTweaksTConstruct/master/version.json");
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        pulsar.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        pulsar.postInit(event);

        FMLCommonHandler.instance().bus().register(new OldToolConversionHandler());
        FMLCommonHandler.instance().bus().register(new AntiChiselDupeHandler());

        GameRegistry.addRecipe(new ToolUpdateRecipe());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        if (pulsar.isPulseLoaded(Reference.PULSE_LEVELING)) {
            Log.debug("Adding command: leveluptool");
            event.registerServerCommand(new IguanaCommandLevelUpTool());
            Log.debug("Adding command: toolxp");
            event.registerServerCommand(new IguanaCommandToolXP());
            event.registerServerCommand(new CommandIAmADirtyCheater());
        }

        Log.debug("Adding command: dumpTools");
        event.registerServerCommand(new CommandDumpTools());
        Log.debug("Adding command: dumpOredict");
        event.registerServerCommand(new CommandDumpOredict());

        if (pulseCFG.isModuleEnabled(new PulseMeta("Debug", "", false, false)))
            event.registerServerCommand(new DebugCommand());
    }

    public static String getHarvestLevelName(int num) {
        return HarvestLevels.getHarvestLevelName(num);
    }
}
