package iguanaman.iguanatweakstconstruct.modcompat.fmp;

import java.lang.reflect.Field;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;

import codechicken.microblock.ItemSaw;
import codechicken.microblock.Saw;
import codechicken.microblock.handler.MicroblockProxy;
import codechicken.multipart.handler.MultipartProxy;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.harvestlevels.HarvestLevelTweaks;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.Log;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.tools.TinkerTools;

@Pulse(
        id = Reference.PULSE_COMPAT_FMP,
        description = "Makes Saw cut stuff again",
        pulsesRequired = Reference.PULSE_HARVESTTWEAKS,
        modsRequired = "ForgeMultipart")
public class IguanaFMPCompat {

    @SidedProxy(
            clientSide = "iguanaman.iguanatweakstconstruct.modcompat.fmp.ClientFMPProxy",
            serverSide = "iguanaman.iguanatweakstconstruct.modcompat.fmp.CommonFMPProxy")
    public static CommonFMPProxy proxy;

    public static Item arditeSaw;
    public static Item cobaltSaw;
    public static Item manyullynSaw;

    private Item createSaw(ToolMaterial mat) {
        String name = String.format("saw%s", mat.name());
        // proper default value...
        MultipartProxy.config().getTag(name).useBraces().getTag("durability").getIntValue(mat.durability() * 5);
        // create the saw
        Item saw = MicroblockProxy.createSaw(MultipartProxy.config(), name, mat.harvestLevel());
        saw.setUnlocalizedName(Reference.prefix(name));

        return saw;
    }

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        if (IguanaTweaksTConstruct.pulsar.isPulseLoaded("Debug"))
            MinecraftForge.EVENT_BUS.register(new SawStrengthHandler());

        Log.debug("Adapting and adding FMP saws");
        // change existing saws
        for (Object o : Item.itemRegistry) {
            // is it a saw?
            if (!(o instanceof Saw)) continue;

            if (o == arditeSaw || o == cobaltSaw || o == manyullynSaw) continue;

            Item item = (Item) o;

            // FMP saw
            if (o instanceof ItemSaw) {
                try {
                    Field hlvlField = ItemSaw.class.getDeclaredField("harvestLevel");
                    hlvlField.setAccessible(true);
                    Integer old = (Integer) hlvlField.get(o);
                    Integer hlvl = HarvestLevelTweaks.getUpdatedHarvestLevel(old);

                    // update the value
                    hlvlField.set(o, hlvl);
                    if (Config.logMiningLevelChanges) Log.info(
                            String.format(
                                    "Changed Cutting Strength for %s from %d to %d",
                                    item.getUnlocalizedName(),
                                    old,
                                    hlvl));
                } catch (Exception e) {
                    // something failed :(
                    Log.error(String.format("Couldn't update FMP saw %s", item.getUnlocalizedName()));
                }
            }
        }

        // create our own saws (have to do this this late, since the harvestlevel changes and overrides have to be
        // applied first)
        arditeSaw = createSaw(TConstructRegistry.getMaterial("Ardite"));
        cobaltSaw = createSaw(TConstructRegistry.getMaterial("Cobalt"));
        manyullynSaw = createSaw(TConstructRegistry.getMaterial("Manyullyn"));

        // and add recipes for them
        String[] recipe = { "srr", "sbr" };
        if (!Loader.isModLoaded("dreamcraft")) {
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(arditeSaw),
                            recipe,
                            's',
                            Items.stick,
                            'r',
                            "rodStone",
                            'b',
                            new ItemStack(TinkerTools.toolRod, 1, 11)));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(cobaltSaw),
                            recipe,
                            's',
                            Items.stick,
                            'r',
                            "rodStone",
                            'b',
                            new ItemStack(TinkerTools.toolRod, 1, 10)));
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(manyullynSaw),
                            recipe,
                            's',
                            Items.stick,
                            'r',
                            "rodStone",
                            'b',
                            new ItemStack(TinkerTools.toolRod, 1, 12)));
        } else {
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(arditeSaw),
                            recipe,
                            's',
                            "stickWood",
                            'r',
                            "stickSteel",
                            'b',
                            "toolHeadSawArdite"));

            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(cobaltSaw),
                            recipe,
                            's',
                            "stickWood",
                            'r',
                            "stickSteel",
                            'b',
                            "toolHeadSawCobalt"));

            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(manyullynSaw),
                            recipe,
                            's',
                            "stickWood",
                            'r',
                            "stickSteel",
                            'b',
                            "toolHeadSawManyullyn"));
        }

        proxy.updateSawRenderers();
    }
}
