package iguanaman.iguanatweakstconstruct.worldgen;

import net.minecraftforge.common.ChestGenHooks;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;

@Pulse(id = Reference.PULSE_WORLDGEN, description = "This module takes care of additional awesome dungeon loot.")
public class IguanaWorldGen {

    @Handler
    public void init(FMLInitializationEvent event) {
        // add some a starting weapon to bonus chests!
        ChestGenHooks.addItem(
                ChestGenHooks.BONUS_CHEST,
                new RandomWeaponChestContent(0, 1, 10, 0, 0, HarvestLevels._1_flint, 3));

        // awesome dungeon loot!
        ChestGenHooks.addItem(
                ChestGenHooks.DUNGEON_CHEST,
                new RandomWeaponChestContent(0, 1, 2, 1, 2, HarvestLevels._5_diamond, 4));
        ChestGenHooks.addItem(
                ChestGenHooks.MINESHAFT_CORRIDOR,
                new RandomWeaponChestContent(0, 1, 2, 0, 0, HarvestLevels._2_copper, 2));
        ChestGenHooks.addItem(
                ChestGenHooks.PYRAMID_DESERT_CHEST,
                new RandomWeaponChestContent(0, 1, 2, 0, 2, HarvestLevels._5_diamond, 3));
        ChestGenHooks.addItem(
                ChestGenHooks.PYRAMID_JUNGLE_CHEST,
                new RandomWeaponChestContent(0, 1, 2, 1, 1, HarvestLevels._5_diamond, 3));
        ChestGenHooks.addItem(
                ChestGenHooks.STRONGHOLD_LIBRARY,
                new RandomWeaponChestContent(0, 1, 2, 2, 2, HarvestLevels._5_diamond, 4));
    }
}
