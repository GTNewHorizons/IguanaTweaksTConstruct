package iguanaman.iguanatweakstconstruct.override;

import java.util.Map;
import java.util.Set;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import iguanaman.iguanatweakstconstruct.leveling.RandomBonuses;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.Log;

public class ModifierOverride implements IOverride {

    @Override
    public void createDefault(Configuration config) {
        Log.debug("Creating Modifier Default File");

        config.get(
                "UsageBonus",
                "bonusWeight",
                RandomBonuses.usageBonusWeight,
                "The average amount of weight added if a tool does one action for a whole levelup.");

        for (RandomBonuses.Modifier mod : RandomBonuses.Modifier.values()) {
            // tool modifiers
            config.get("ToolWeights", mod.toString(), RandomBonuses.toolWeights.get(mod));
            // weapon modifiers
            config.get("WeaponWeights", mod.toString(), RandomBonuses.weaponWeights.get(mod));
            // bow modifiers
            config.get("BowWeights", mod.toString(), RandomBonuses.bowWeights.get(mod));
            // ammo modifiers
            config.get("AmmoWeights", mod.toString(), RandomBonuses.ammoWeights.get(mod));

            // useful modifiers
            config.get("UsefulToolBonuses", mod.toString(), RandomBonuses.usefulToolModifiers.contains(mod));
            config.get("UsefulWeaponBonuses", mod.toString(), RandomBonuses.usefulWeaponModifiers.contains(mod));
            config.get("UsefulBowBonuses", mod.toString(), RandomBonuses.usefulBowModifiers.contains(mod));
            config.get("UsefulAmmoBonuses", mod.toString(), RandomBonuses.usefulAmmoModifiers.contains(mod));
        }
    }

    @Override
    public void processConfig(Configuration config) {
        Log.debug("Loading Modifier Overrides");

        StringBuilder comment = new StringBuilder();
        comment.append("Tools, Weapons and Bows use separate data to determine which modifier should be rewarded.\n");
        comment.append(
                "The weights work like this: All weights are summed up to a total. then a random number in between is picked, and depending on where the number is, that modifier is picked.\n");
        comment.append("So basically: Modifier A has a weight of 5, Modifier B has a weight of 20. Sum is 25.\n");
        comment.append(
                "  -> Modifier A is B times more likely than modifier A, since a number between 0 and 25 has 5/25 chance to be modifier A, but 20/25 chance to be modifier B.\n");
        comment.append(
                "Additionally a bonus is added to the different weights depending on the tool usage. How big that bonus is, is defined by the usageWeight.");

        config.setCategoryComment(" Info", comment.toString());

        config.setCategoryComment(
                "UsageBonus",
                "Tools gain a bonus for specific modifiers on doing specific things.\nThe value below determines how much weight is added (on average) to a modifier if only that action is done for the whole level.\nAn example: Mining blocks increases the chance to obtain the redstone modifier. If you'd only mine stone blocks from 0xp to levelup, the weigth of the redstone modifier woudl be increased by that amount. (That's why it's relatively low by default)");

        RandomBonuses.usageBonusWeight = config.get(
                "UsageBonus",
                "bonusWeight",
                RandomBonuses.usageBonusWeight,
                "The average amount of weight added if a tool does one action for a whole levelup.").getInt();
        if (Config.logOverrideChanges)
            Log.info(String.format("Bonus Modifier Override: Set bonus weight to %d", RandomBonuses.usageBonusWeight));

        // tool weights
        doWeightUpdate(config, "ToolWeights", RandomBonuses.toolWeights);
        doWeightUpdate(config, "WeaponWeights", RandomBonuses.weaponWeights);
        doWeightUpdate(config, "BowWeights", RandomBonuses.bowWeights);
        doWeightUpdate(config, "AmmoWeights", RandomBonuses.ammoWeights);

        // useful bonuses
        for (RandomBonuses.Modifier mod : RandomBonuses.Modifier.values()) {
            doUsefulnessUpdate(config, "UsefulToolBonuses", mod, RandomBonuses.usefulToolModifiers);
            doUsefulnessUpdate(config, "UsefulWeaponBonuses", mod, RandomBonuses.usefulWeaponModifiers);
            doUsefulnessUpdate(config, "UsefulBowBonuses", mod, RandomBonuses.usefulBowModifiers);
            doUsefulnessUpdate(config, "UsefulAmmoBonuses", mod, RandomBonuses.usefulAmmoModifiers);
        }
    }

    private void doWeightUpdate(Configuration config, String categoryName, Map<RandomBonuses.Modifier, Integer> map) {
        ConfigCategory cat = config.getCategory(categoryName);
        for (Property prop : cat.values()) {
            try {
                RandomBonuses.Modifier mod = RandomBonuses.Modifier.getEnumByString(prop.getName());
                int weight = Math.max(0, prop.getInt());
                map.put(mod, weight);

                if (Config.logOverrideChanges) Log.info(
                        String.format(
                                "Bonus Modifier Override: [%s] Changed Weight of %s to %d",
                                categoryName,
                                mod.toString(),
                                prop.getInt()));
            } catch (IllegalArgumentException e) {
                Log.error(String.format("Found invalid entry when parsing %s: %s", categoryName, prop.getName()));
            }
        }
    }

    private void doUsefulnessUpdate(Configuration config, String category, RandomBonuses.Modifier mod,
            Set<RandomBonuses.Modifier> set) {
        boolean useful = config.get(category, mod.toString(), set.contains(mod)).getBoolean();
        if (useful) {
            set.add(mod);
            if (Config.logOverrideChanges) Log.info(
                    String.format("Bonus Modifier Override: [%s] Added useful modifier %s", category, mod.toString()));
        } else {
            set.remove(mod);
            if (Config.logOverrideChanges) Log.info(
                    String.format(
                            "Bonus Modifier Override: [%s] Removed modifier %s from useful modifiers",
                            category,
                            mod.toString()));
        }
    }
}
