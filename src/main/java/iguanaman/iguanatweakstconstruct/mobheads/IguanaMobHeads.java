package iguanaman.iguanatweakstconstruct.mobheads;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.mobheads.blocks.IguanaSkullBlock;
import iguanaman.iguanatweakstconstruct.mobheads.handlers.MobHeadHandler;
import iguanaman.iguanatweakstconstruct.mobheads.items.IguanaSkull;
import iguanaman.iguanatweakstconstruct.mobheads.items.Wearable;
import iguanaman.iguanatweakstconstruct.mobheads.proxy.MobHeadCommonProxy;
import iguanaman.iguanatweakstconstruct.mobheads.tileentities.IguanaSkullTileEntity;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.Log;
import iguanaman.iguanatweakstconstruct.util.ModSupportHelper;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;

/**
 * Adds additional MobHeads and controls MobHead dropping.
 */
@Pulse(id = Reference.PULSE_MOBHEADS, description = "Adds additional MobHeads and control over MobHead drops.")
public class IguanaMobHeads {
    public static Item skullItem;
    public static Block skullBlock;
    public static Item wearables; // secret thing

    @SidedProxy(
            clientSide = "iguanaman.iguanatweakstconstruct.mobheads.proxy.MobHeadClientProxy",
            serverSide = "iguanaman.iguanatweakstconstruct.mobheads.proxy.MobHeadCommonProxy")
    public static MobHeadCommonProxy proxy;

    @Handler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.initialize();

        if (ModSupportHelper.ThermalFoundation) integrateThermalExpansion();

        skullItem = new IguanaSkull();
        GameRegistry.registerItem(skullItem, "skullItem");

        skullBlock = new IguanaSkullBlock();
        GameRegistry.registerBlock(skullBlock, "skullBlock");
        GameRegistry.registerTileEntity(IguanaSkullTileEntity.class, "skullTE");

        // psssssst!
        wearables = new Wearable();
        GameRegistry.registerItem(wearables, "wearableBucket");
    }

    @Handler
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new MobHeadHandler());

        proxy.postInit();

        // :>
        ChestGenHooks.addItem(
                ChestGenHooks.VILLAGE_BLACKSMITH,
                new WeightedRandomChestContent(new ItemStack(wearables, 1, 0), 0, 1, 1));
        ChestGenHooks.addItem(
                ChestGenHooks.STRONGHOLD_LIBRARY,
                new WeightedRandomChestContent(new ItemStack(wearables, 1, 3), 0, 1, 1));
    }

    private void integrateThermalExpansion() {
        Log.debug("Adding Blizz head");
        IguanaSkull.addHead(3, "blizz", "skull_blizz");
    }
}
