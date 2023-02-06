package iguanaman.iguanatweakstconstruct.leveling;

import java.util.List;
import java.util.ListIterator;

import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.tools.ToolCore;
import tconstruct.modifiers.tools.ModRedstone;
import tconstruct.modifiers.tools.ModWindup;
import tconstruct.tools.TinkerTools;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.leveling.handlers.LevelingEventHandler;
import iguanaman.iguanatweakstconstruct.leveling.handlers.LevelingToolTipHandler;
import iguanaman.iguanatweakstconstruct.leveling.handlers.MobHeadTooltipHandler;
import iguanaman.iguanatweakstconstruct.leveling.modifiers.ModMiningLevelBoost;
import iguanaman.iguanatweakstconstruct.leveling.modifiers.ModXpAwareRedstone;
import iguanaman.iguanatweakstconstruct.mobheads.IguanaMobHeads;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;
import iguanaman.iguanatweakstconstruct.util.ModSupportHelper;

/**
 * The Leveling Pulse. If Leveling were a separate mod instead of pulse-model, this'd be a @Mod This pulse contains all
 * the stuff that has to do with tool leveling.
 */
@Pulse(id = Reference.PULSE_LEVELING, description = "The Iguana Tweaks Leveling System for Tinker's Tools")
public class IguanaToolLeveling {

    public static Item rubberChicken;

    @Handler
    public void preInit(FMLPreInitializationEvent event) {
        rubberChicken = new Item();
        rubberChicken.setTextureName(Reference.resource("rubber_chicken"));
        rubberChicken.setUnlocalizedName(Reference.prefix("rubberChicken"));

        GameRegistry.registerItem(rubberChicken, "rubberChicken");

        if (event.getSide() == Side.CLIENT && Loader.isModLoaded("NotEnoughItems")) {
            // sneaky rubber chicken
            codechicken.nei.api.API.hideItem(new ItemStack(rubberChicken));
        }
    }

    @Handler
    public void init(FMLInitializationEvent event) {
        // has to be before the original toolmod, otherwise we can't reward xp because of some modifications it does
        TConstructRegistry.activeModifiers.add(0, new LevelingActiveToolMod());

        // substitute modifiers that need an xp-aware implementation
        takeOverModifiers();

        // mobhead modifiers for mining boost
        registerBoostModifiers();
    }

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        LevelingEventHandler handler = new LevelingEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
        MinecraftForge.EVENT_BUS.register(new LevelingToolTipHandler());
        if (Config.mobHeadPickaxeBoost) MinecraftForge.EVENT_BUS.register(new MobHeadTooltipHandler());
    }

    // replace modifiers with our own, adjusted, modifiers
    private void takeOverModifiers() {
        List<ItemModifier> mods = ModifyBuilder.instance.itemModifiers;
        for (ListIterator<ItemModifier> iter = mods.listIterator(); iter.hasNext();) {
            ItemModifier mod = iter.next();
            // redstone
            if (mod instanceof ModRedstone && !(mod instanceof ModWindup)) {
                iter.set(new ModXpAwareRedstone((ModRedstone) mod));
                Log.trace("Replaced Redstone Modifier");
            }
        }
    }

    // registers the available mobheads as a mining-boost modifier
    private void registerBoostModifiers() {
        // zombie head
        ModifyBuilder.registerModifier(new ModMiningLevelBoost(getVanillaMobHead(2), 20, HarvestLevels._2_copper));
        // skeleton skull
        ModifyBuilder.registerModifier(new ModMiningLevelBoost(getVanillaMobHead(0), 21, HarvestLevels._3_iron));
        // creeper head
        ModifyBuilder.registerModifier(new ModMiningLevelBoost(getVanillaMobHead(4), 22, HarvestLevels._5_diamond));

        if (IguanaTweaksTConstruct.pulsar.isPulseLoaded(Reference.PULSE_MOBHEADS)) {
            // pigman head
            ModifyBuilder.registerModifier(new ModMiningLevelBoost(getIguanaMobHead(1), 23, HarvestLevels._5_diamond));
            // blaze head
            ModifyBuilder.registerModifier(new ModMiningLevelBoost(getIguanaMobHead(2), 24, HarvestLevels._6_obsidian));
            // blizz head
            if (ModSupportHelper.ThermalFoundation) ModifyBuilder
                    .registerModifier(new ModMiningLevelBoost(getIguanaMobHead(3), 25, HarvestLevels._6_obsidian));
            // enderman head
            ModifyBuilder.registerModifier(new ModMiningLevelBoost(getIguanaMobHead(0), 26, HarvestLevels._7_ardite));
        }

        // wither head
        ModifyBuilder.registerModifier(new ModMiningLevelBoost(getVanillaMobHead(1), 27, HarvestLevels._8_cobalt));
        // netherstar
        ModifyBuilder.registerModifier(
                new ModMiningLevelBoost(
                        new ItemStack[] { new ItemStack(Items.nether_star) },
                        28,
                        HarvestLevels._9_manyullym));

        // rendering code
        ToolCore[] tools = new ToolCore[] { TinkerTools.pickaxe, TinkerTools.hammer };
        int[] modifierIds = new int[] { 20, 21, 22, 23, 24, 25, 26, 27, 28 };
        String[] renderNames = new String[] { "zombiehead", "skeletonskull", "creeperhead", "zombiepigmanhead",
                "blazehead", "blizzhead", "endermanhead", "witherskeletonskull", "netherstar" };

        for (ToolCore tool : tools) for (int index = 0; index < modifierIds.length; ++index) TConstructClientRegistry
                .addEffectRenderMapping(tool, modifierIds[index], Reference.RESOURCE, renderNames[index], true);
    }

    private ItemStack[] getVanillaMobHead(int meta) {
        return new ItemStack[] { new ItemStack(Items.skull, 1, meta) };
    }

    private ItemStack[] getIguanaMobHead(int meta) {
        return new ItemStack[] { new ItemStack(IguanaMobHeads.skullItem, 1, meta) };
    }
}
