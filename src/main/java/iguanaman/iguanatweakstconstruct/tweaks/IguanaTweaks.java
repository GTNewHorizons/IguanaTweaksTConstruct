package iguanaman.iguanatweakstconstruct.tweaks;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.tweaks.handlers.*;
import iguanaman.iguanatweakstconstruct.tweaks.modifiers.ModFluxExpensive;
import iguanaman.iguanatweakstconstruct.tweaks.modifiers.ModLimitedToolRepair;
import iguanaman.iguanatweakstconstruct.util.Log;
import java.util.*;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import mantle.utils.RecipeRemover;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.crafting.*;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.util.IPattern;
import tconstruct.modifiers.tools.ModExtraModifier;
import tconstruct.modifiers.tools.ModFlux;
import tconstruct.modifiers.tools.ModToolRepair;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.tools.TinkerTools;
import tconstruct.world.TinkerWorld;

/**
 * Various Tweaks for Tinkers Construct and Vanilla Minecraft. See Config.
 */
@Pulse(
        id = Reference.PULSE_TWEAKS,
        description = "Various Tweaks for vanilla Minecraft and Tinker's Construct. See Config.")
public class IguanaTweaks {
    public static Set<Item> toolWhitelist = new HashSet<Item>();

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        // flint recipes n stuff
        flintTweaks();

        // add string bindings. yay.
        if (Config.allowStringBinding) {
            Log.debug("Register String binding");
            TConstructRegistry.addToolMaterial(
                    40, "String", 0, 33, 1, 0, 0.01F, 0, 0f, EnumChatFormatting.WHITE.toString(), 0xFFFFFF);
            TConstructClientRegistry.addMaterialRenderMapping(40, "tinker", "paper", true);
            MinecraftForge.EVENT_BUS.register(new StringBindingHandler());
        }

        if (Config.allowStencilReuse) {
            Log.debug("Make stencils reusable");
            for (ItemStack stack : StencilBuilder.getStencils()) StencilBuilder.registerBlankStencil(stack);
        }

        if (Config.castsBurnMaterial) {
            Log.debug("Burn casting materials to a crisp");
            MinecraftForge.EVENT_BUS.register(new CastHandler());
        }

        if (Config.allowPartReuse) reusableToolParts();

        // no stone tools for you
        if (Config.disableStoneTools) {
            Log.debug("Disabling tinkers stone tools");
            MinecraftForge.EVENT_BUS.register(new StoneToolHandler());
            ChestGenHooks.removeItem(ChestGenHooks.BONUS_CHEST, new ItemStack(Items.stone_axe));
            ChestGenHooks.removeItem(ChestGenHooks.BONUS_CHEST, new ItemStack(Items.stone_pickaxe));
        }

        // because diamond pickaxe is hax
        if (Config.nerfVanillaTools) {
            // init whitelist
            findToolsFromConfig();

            Log.info("Sticks and stones may break my bones, but your pickaxes and axes will break no blocks.");
            MinecraftForge.EVENT_BUS.register(new VanillaToolNerfHandler());

            // replace vanilla tools with tinker tools in bonus chests
            ChestGenHooks.removeItem(ChestGenHooks.BONUS_CHEST, new ItemStack(Items.wooden_pickaxe));
            ChestGenHooks.removeItem(ChestGenHooks.BONUS_CHEST, new ItemStack(Items.wooden_axe));
            ItemStack starterPick = ToolBuilder.instance.buildTool(
                    new ItemStack(TinkerTools.pickaxeHead, 1, 0),
                    new ItemStack(TinkerTools.toolRod, 1, 0),
                    new ItemStack(TinkerTools.binding, 1, 0),
                    "Starter Pickaxe");
            ItemStack starterAxe = ToolBuilder.instance.buildTool(
                    new ItemStack(TinkerTools.hatchetHead, 1, 0),
                    new ItemStack(TinkerTools.toolRod, 1, 0),
                    null,
                    "Starter Hatchet");
            if (starterPick != null)
                ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, new WeightedRandomChestContent(starterPick, 1, 1, 5));
            if (starterAxe != null)
                ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, new WeightedRandomChestContent(starterAxe, 1, 1, 5));

            // same with stone tools if not disabled
            if (!Config.disableStoneTools) {
                ChestGenHooks.removeItem(ChestGenHooks.BONUS_CHEST, new ItemStack(Items.stone_axe));
                ChestGenHooks.removeItem(ChestGenHooks.BONUS_CHEST, new ItemStack(Items.stone_pickaxe));
                ItemStack stonePick = ToolBuilder.instance.buildTool(
                        new ItemStack(TinkerTools.pickaxeHead, 1, 1),
                        new ItemStack(TinkerTools.toolRod, 1, 0),
                        new ItemStack(TinkerTools.binding, 1, 0),
                        "");
                ItemStack stoneAxe = ToolBuilder.instance.buildTool(
                        new ItemStack(TinkerTools.hatchetHead, 1, 1),
                        new ItemStack(TinkerTools.toolRod, 1, 0),
                        null,
                        "");
                if (stonePick != null)
                    ChestGenHooks.addItem(
                            ChestGenHooks.BONUS_CHEST, new WeightedRandomChestContent(stonePick, 1, 1, 5));
                if (stoneAxe != null)
                    ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, new WeightedRandomChestContent(stoneAxe, 1, 1, 5));
            }
        }

        // no hoes for you
        if (Config.nerfVanillaHoes) {
            Log.debug("Vanilla hoe? More like vanilla go!");
            MinecraftForge.EVENT_BUS.register(new VanillaHoeNerfHandler());
        }

        if (Config.nerfVanillaSwords) {
            Log.debug("Replacing swords with pasta");
            MinecraftForge.EVENT_BUS.register(new VanillaSwordNerfHandler());
        }

        if (Config.nerfVanillaBows) {
            Log.debug("Sabotaging bows");
            MinecraftForge.EVENT_BUS.register(new VanillaBowNerfHandler());
        }

        // stonetorches
        if (Config.removeStoneTorchRecipe) {
            Log.debug("Removing stone torch recipe");
            RecipeRemover.removeAnyRecipe(new ItemStack(TinkerWorld.stoneTorch, 4));
        }

        // silky jewel nerfs
        if (Config.moreExpensiveSilkyCloth) {
            Log.debug("Making Silky Cloth more expensive");
            RecipeRemover.removeAnyRecipe(new ItemStack(TinkerTools.materials, 1, 25));
            String[] patSurround = {"###", "#m#", "###"};
            GameRegistry.addRecipe(
                    new ItemStack(TinkerTools.materials, 1, 25),
                    patSurround,
                    'm',
                    new ItemStack(TinkerTools.materials, 1, 14),
                    '#',
                    new ItemStack(Items.string));
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(TinkerTools.materials, 1, 25),
                    patSurround,
                    'm',
                    "ingotGold",
                    '#',
                    new ItemStack(Items.string)));
        }
        if (Config.moreExpensiveSilkyJewel) {
            Log.debug("Making Silky Jewel more expensive");
            RecipeRemover.removeAnyRecipe(new ItemStack(TinkerTools.materials, 1, 26));
            GameRegistry.addRecipe(
                    new ItemStack(TinkerTools.materials, 1, 26),
                    " c ",
                    "cec",
                    " c ",
                    'c',
                    new ItemStack(TinkerTools.materials, 1, 25),
                    'e',
                    new ItemStack(Item.getItemFromBlock(Blocks.emerald_block)));
        }

        if (Config.moreModifiersForFlux) exchangeFluxModifier();

        if (Config.disableBonusMods) removeBonusModifierModifiers();

        if (Config.maxToolRepairs > -1) limitToolRepair();

        // has to be added after exchanging the repair modifier, to obtain the correct cache
        if (Config.easyToolRepair) GameRegistry.addRecipe(new RepairCraftingRecipe());

        if (Config.easyPartCrafting) GameRegistry.addRecipe(new PartCraftingRecipe());

        if (Config.easyToolBuilding) GameRegistry.addRecipe(new ToolCraftingRecipe());

        if (Config.removeObsidianAlloy) removeObsidianAlloy();
    }

    private void flintTweaks() {
        if (Config.removeFlintDrop) {
            Log.debug("Removing Flint drops from Gravel");
            MinecraftForge.EVENT_BUS.register(new FlintHandler());
        }

        if (Config.addFlintRecipe) {
            Log.debug("Adding shapeless Flint recipe from " + Config.recipeGravelPerFlint + " Gravel");
            // create recipe
            Object[] recipe = new ItemStack[Config.recipeGravelPerFlint];
            for (int i = 0; i < Config.recipeGravelPerFlint; i++) recipe[i] = new ItemStack(Blocks.gravel);

            // add recipe
            GameRegistry.addShapelessRecipe(new ItemStack(Items.flint), recipe);
        }
    }

    private void reusableToolParts() {
        Log.debug("Registering reusable tool parts");
        // the material IDs of non-metal parts
        // int[] nonMetals = { 0, 1, 3, 4, 5, 6, 7, 8, 9, 17, 31 };
        for (Map.Entry<List, ItemStack> entry : TConstructRegistry.patternPartMapping.entrySet()) {
            Item pattern = (Item) entry.getKey().get(0); // the pattern
            Integer meta = (Integer) entry.getKey().get(1); // metadata of the pattern
            Integer matID = (Integer) entry.getKey().get(2); // Material-ID of the material needed to craft
            ItemStack toolPart = (ItemStack) entry.getValue(); // the itemstack created

            // get pattern cost
            int cost = ((IPattern) pattern).getPatternCost(new ItemStack(pattern, 1, meta)); // the cost is 0.5*2
            if (cost <= 0) continue;

            PatternBuilder.instance.registerMaterial(
                    toolPart, cost, TConstructRegistry.getMaterial(matID).materialName);
        }
    }

    private void exchangeFluxModifier() {

        List<ItemModifier> mods = ModifyBuilder.instance.itemModifiers;
        for (ListIterator<ItemModifier> iter = mods.listIterator(); iter.hasNext(); ) {
            ItemModifier mod = iter.next();
            // flux mod
            if (mod instanceof ModFlux) {
                iter.set(new ModFluxExpensive(((ModFlux) mod).batteries));
                Log.debug("Replaced Flux Modifier to make it more expensive");
            }
        }
    }

    private void removeBonusModifierModifiers() {
        Log.debug("Removing bonus modifier modifiers");
        List<ItemModifier> mods = ModifyBuilder.instance.itemModifiers;
        for (ListIterator<ItemModifier> iter = mods.listIterator(); iter.hasNext(); ) {
            ItemModifier mod = iter.next();
            // flux mod
            if (mod instanceof ModExtraModifier) {
                iter.remove();
            }
        }
    }

    private void limitToolRepair() {

        List<ItemModifier> mods = ModifyBuilder.instance.itemModifiers;
        for (ListIterator<ItemModifier> iter = mods.listIterator(); iter.hasNext(); ) {
            ItemModifier mod = iter.next();
            // flux mod
            if (mod instanceof ModToolRepair) {
                iter.set(new ModLimitedToolRepair());
                Log.debug("Replaced Tool Repair Modifier to limit the maximum amount of repairs");
            }
        }
    }

    private static void findToolsFromConfig() {
        Log.debug("Setting up whitelist/blacklist for allowed tools");

        // cycle through all items
        for (Object identifier : Item.itemRegistry.getKeys()) {
            Object item = Item.itemRegistry.getObject(identifier);
            // do we care about this item?
            if (!(item instanceof ItemTool
                    || item instanceof ItemHoe
                    || item instanceof ItemSword
                    || item instanceof ItemBow)) continue;

            String mod = identifier.toString().split(":")[0]; // should always be non-null... I think

            // whitelist
            if (Config.excludedToolsIsWhitelist) {
                // on the whitelist?
                if (Config.excludedModTools.contains(mod) || Config.excludedTools.contains(identifier))
                    toolWhitelist.add((Item) item);
            }
            // blacklist
            else {
                if (!Config.excludedModTools.contains(mod) && !Config.excludedTools.contains(identifier))
                    toolWhitelist.add((Item) item);
            }
        }
    }

    private static void removeObsidianAlloy() {
        if (TinkerSmeltery.moltenObsidianFluid == null) return;

        Log.debug("Removing Obsidian alloy");

        ListIterator<AlloyMix> iter = Smeltery.getAlloyList().listIterator();

        while (iter.hasNext()) {
            AlloyMix mix = iter.next();
            // remove the alloy if the result is obsidian
            if (mix.result.getFluid() == TinkerSmeltery.moltenObsidianFluid) iter.remove();
        }
    }
}
