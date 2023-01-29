package iguanaman.iguanatweakstconstruct.harvestlevels;

import java.util.Map;

import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import tconstruct.TConstruct;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.tools.TinkerTools;
import tconstruct.util.config.PHConstruct;
import tconstruct.world.TinkerWorld;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import iguanaman.iguanatweakstconstruct.harvestlevels.modifiers.ModBonusMiningLevel;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;

/**
 * The Harvest-Tweaks Pulse. If this were a separate mod instead of pulse-module, it'd be a @Mod This pulse modifies the
 * harvest level of all tools and blocks. Check util.HarvestLevels for more info.
 *
 * Check the oreDictlevels to get an idea of what can be harvested with each tier.
 */

@Pulse(
        id = Reference.PULSE_HARVESTTWEAKS,
        description = "Modify tool and item mining levels to create a tiered-ish progression")
public class IguanaHarvestLevelTweaks {

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        TinkerMaterialTweaks.modifyToolMaterials();
        HarvestLevelTweaks.modifyHarvestLevels();
        MinecraftForge.EVENT_BUS.register(new VanillaToolTipHandler());

        if (Config.changeDiamondModifier) changeDurabilityModifiers();

        if (TConstruct.pulsar.isPulseLoaded("Tinkers' World")) adaptChestLoot();
    }

    // removes all pickaxe and hammer heads that have a higher harvestlevel than flint
    private void adaptChestLoot() {
        for (Map.Entry<Integer, ToolMaterial> mat : TConstructRegistry.toolMaterials.entrySet()) {
            // we allow flint and lower
            if (mat.getValue().harvestLevel() <= HarvestLevels._1_flint) continue;

            // remove pickaxe head
            TinkerWorld.tinkerHouseChest.removeItem(new ItemStack(TinkerTools.pickaxeHead, 1, mat.getKey()));
            // remove hammer head
            TinkerWorld.tinkerHouseChest.removeItem(new ItemStack(TinkerTools.hammerHead, 1, mat.getKey()));
            // shovel
            TinkerWorld.tinkerHouseChest.removeItem(new ItemStack(TinkerTools.shovelHead, 1, mat.getKey()));
            // excavator
            TinkerWorld.tinkerHouseChest.removeItem(new ItemStack(TinkerTools.excavatorHead, 1, mat.getKey()));
            // hatchet
            TinkerWorld.tinkerHouseChest.removeItem(new ItemStack(TinkerTools.hatchetHead, 1, mat.getKey()));
            // lumberaxe
            TinkerWorld.tinkerHouseChest.removeItem(new ItemStack(TinkerTools.broadAxeHead, 1, mat.getKey()));
            // other stuff should be fine
        }
    }

    private void changeDurabilityModifiers() {
        // deactivate mininglevel increase by tcon
        PHConstruct.miningLevelIncrease = false;

        Log.debug("Adding Diamond/Emerald Modifiers for Mining Levels");
        ModifyBuilder
                .registerModifier(new ModBonusMiningLevel(new ItemStack[] { new ItemStack(Items.diamond) }, "Diamond"));
        ModifyBuilder
                .registerModifier(new ModBonusMiningLevel(new ItemStack[] { new ItemStack(Items.emerald) }, "Emerald"));
    }

}
