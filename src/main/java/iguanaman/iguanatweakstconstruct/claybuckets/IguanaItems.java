package iguanaman.iguanatweakstconstruct.claybuckets;

import static tconstruct.smeltery.TinkerSmeltery.*;

import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.world.TinkerWorld;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.claybuckets.items.ClayBucket;
import iguanaman.iguanatweakstconstruct.claybuckets.items.ClayBucketMilk;
import iguanaman.iguanatweakstconstruct.claybuckets.items.ClayBucketTinkerLiquids;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.Log;

@Pulse(id = Reference.PULSE_ITEMS, description = "All the Items Iguana Tweaks for TConstruct adds (Clay Buckets,...)")
public class IguanaItems {

    public static Item clayBucketUnfired;
    public static Item clayBucketFired;
    public static Item clayBucketWater;
    public static Item clayBucketLava;
    public static Item clayBucketMilk;
    public static Item clayBucketsTinkers;

    @Handler
    public void preInit(FMLPreInitializationEvent event) {
        Log.debug("Adding Items");
        // unfired clay bucket is a regular item
        clayBucketUnfired = new Item().setUnlocalizedName(Reference.prefix("clayBucketUnfired"))
                .setTextureName(Reference.resource("clayBucketUnfired")).setMaxStackSize(16)
                .setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(clayBucketUnfired, "clayBucketUnfired");

        clayBucketFired = new ClayBucket(Blocks.air, "clayBucketFired", "clayBucketFired").setMaxStackSize(16);
        clayBucketWater = new ClayBucket(Blocks.flowing_water, "clayBucket.Water", "clayBucketWater");
        clayBucketLava = new ClayBucket(Blocks.flowing_lava, "clayBucket.Lava", "clayBucketLava");
        clayBucketMilk = new ClayBucketMilk();

        clayBucketsTinkers = new ClayBucketTinkerLiquids(null);

        GameRegistry.registerItem(clayBucketFired, "clayBucketFired");
        GameRegistry.registerItem(clayBucketWater, "clayBucketWater");
        GameRegistry.registerItem(clayBucketLava, "clayBucketLava");
        GameRegistry.registerItem(clayBucketMilk, "clayBucketMilk");
        GameRegistry.registerItem(clayBucketsTinkers, "clayBucketsTinkers");

        // register milkbucket to the ordictionary
        OreDictionary.registerOre("listAllmilk", clayBucketMilk); // i suppose this is for pams harvestcraft.
        OreDictionary.registerOre("listAllwater", clayBucketWater); // also water

        // register the buckets with the fluid container registry
        ItemStack emptyClayBucket = new ItemStack(clayBucketFired);
        FluidContainerRegistry
                .registerFluidContainer(FluidRegistry.WATER, new ItemStack(clayBucketWater), emptyClayBucket);
        FluidContainerRegistry.registerFluidContainer(
                FluidRegistry.LAVA,
                new ItemStack(clayBucketLava),
                new ItemStack(clayBucketFired, 0));

        // only integrate tcon metals if they actually exist
        if (TinkerSmeltery.buckets != null) {
            // tinker metals
            Fluid[] tinkerFluids = new Fluid[] { moltenIronFluid, moltenGoldFluid, moltenCopperFluid, moltenTinFluid,
                    moltenAluminumFluid, moltenCobaltFluid, moltenArditeFluid, moltenBronzeFluid, moltenAlubrassFluid,
                    moltenManyullynFluid, moltenAlumiteFluid, moltenObsidianFluid, moltenSteelFluid, moltenGlassFluid,
                    moltenStoneFluid, moltenEmeraldFluid, bloodFluid, moltenNickelFluid, moltenLeadFluid,
                    moltenSilverFluid, moltenShinyFluid, moltenInvarFluid, moltenElectrumFluid, moltenEnderFluid,
                    TinkerWorld.blueSlimeFluid, glueFluid, pigIronFluid };

            for (int i = 0; i < tinkerFluids.length; i++) if (tinkerFluids[i] != null) FluidContainerRegistry
                    .registerFluidContainer(tinkerFluids[i], new ItemStack(clayBucketsTinkers, 1, i), emptyClayBucket);
        }

        // add recipes
        if (!Loader.isModLoaded("dreamcraft")) {
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(clayBucketUnfired),
                            "c c",
                            " c ",
                            'c',
                            new ItemStack(Items.clay_ball)));
        }
        GameRegistry.addSmelting(clayBucketUnfired, new ItemStack(clayBucketFired), 0.0F);

        Log.debug("Added Items");
    }

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        if (TinkerSmeltery.buckets == null) return;

        MinecraftForge.EVENT_BUS.register(new ClayBucketHandler());
    }
}
