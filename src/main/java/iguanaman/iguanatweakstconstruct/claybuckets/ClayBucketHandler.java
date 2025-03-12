package iguanaman.iguanatweakstconstruct.claybuckets;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.claybuckets.items.BaseClayBucket;
import tconstruct.smeltery.TinkerSmeltery;

public class ClayBucketHandler {

    private static final Map<Block, Item> customClayBuckets = new IdentityHashMap<>();

    /**
     * Register a clay bucket for a given fluid and it's fluid block.
     *
     * @param itemName   The name of the item, this will be used to load its texture and add it to the item registry
     * @param langKey    The full lang key for the displayed name of this bucket
     * @param texture    The full path to the texture for this bucket
     * @param fluid      The fluid this bucket contains
     * @param fluidBlock The fluid block this bucket can pick up and place
     * @return The clay bucket item that was added to the game registry
     */
    public static Item registerClayBucket(String itemName, String langKey, String texture, Fluid fluid,
            Block fluidBlock) {
        Item newClayBucket = new BaseClayBucket(fluidBlock, langKey, texture);
        GameRegistry.registerItem(newClayBucket, itemName);
        FluidContainerRegistry.registerFluidContainer(
                fluid,
                new ItemStack(newClayBucket),
                new ItemStack(IguanaItems.clayBucketFired));
        customClayBuckets.put(fluidBlock, newClayBucket);
        return newClayBucket;
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.world.getBlock(event.x, event.y, event.z);
        if (!(block instanceof BlockCauldron)) {
            return;
        }

        EntityPlayer player = event.entityPlayer;
        if (player == null) {
            return;
        }

        ItemStack equipped = player.getCurrentEquippedItem();
        if (equipped == null || equipped.getItem() != IguanaItems.clayBucketWater) {
            return;
        }

        // if cauldron is full
        if (event.world.getBlockMetadata(event.x, event.y, event.z) == 3) {
            return;
        }

        if (!player.capabilities.isCreativeMode) {
            player.inventory
                    .setInventorySlotContents(player.inventory.currentItem, new ItemStack(IguanaItems.clayBucketFired));
        }

        // func_150024_a = setWaterLevel
        ((BlockCauldron) block).func_150024_a(event.world, event.x, event.y, event.z, 3);
    }

    // milking cows
    @SubscribeEvent
    public void EntityInteract(EntityInteractEvent event) {
        if (event.target == null || !(event.target instanceof EntityCow)) return;
        if (event.entityPlayer == null) return;

        ItemStack equipped = event.entityPlayer.getCurrentEquippedItem();
        // bucket present?
        if (equipped == null || equipped.getItem() != IguanaItems.clayBucketFired) return;

        EntityPlayer player = event.entityPlayer;

        if (equipped.stackSize-- == 1) {
            player.inventory
                    .setInventorySlotContents(player.inventory.currentItem, new ItemStack(IguanaItems.clayBucketMilk));
        } else if (!player.inventory.addItemStackToInventory(new ItemStack(IguanaItems.clayBucketMilk))) {
            player.dropPlayerItemWithRandomChoice(new ItemStack(IguanaItems.clayBucketMilk, 1, 0), false);
        }
    }

    // filling buckets with molten metal, same behaviour as regular buckets from TConstruct, but with clay buckets
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void bucketFill(FillBucketEvent event) {
        if (event.current.getItem() == IguanaItems.clayBucketFired
                && event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            Block block = event.world.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ);
            if (Block.isEqualTo(block, Block.getBlockFromName("tile.blockFuel"))
                    || Block.isEqualTo(block, Block.getBlockFromName("tile.blockOil"))
                    || Block.isEqualTo(block, Block.getBlockFromName("tile.railcraft.fluid.creosote"))) {
                event.result = new ItemStack(IguanaItems.clayBucketFired);
                event.setResult(Event.Result.DENY);
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
            }

            if (event.getResult() != Event.Result.DEFAULT) return;

            int hitX = event.target.blockX;
            int hitY = event.target.blockY;
            int hitZ = event.target.blockZ;

            if (event.entityPlayer != null
                    && !event.entityPlayer.canPlayerEdit(hitX, hitY, hitZ, event.target.sideHit, event.current)) {
                return;
            }

            Block bID = event.world.getBlock(hitX, hitY, hitZ);

            // tinkers fluids
            for (int id = 0; id < TinkerSmeltery.fluidBlocks.length; id++) {
                if (bID == TinkerSmeltery.fluidBlocks[id]) {
                    event.world.setBlockToAir(hitX, hitY, hitZ);
                    if (!event.entityPlayer.capabilities.isCreativeMode) {
                        event.world.setBlockToAir(hitX, hitY, hitZ);

                        event.setResult(Event.Result.ALLOW);
                        event.result = new ItemStack(IguanaItems.clayBucketsTinkers, 1, id);
                        return;
                    }
                }
            }

            // water and lava
            if (bID == Blocks.water || bID == Blocks.flowing_water) {
                event.setResult(Event.Result.ALLOW);
                event.result = new ItemStack(IguanaItems.clayBucketWater);
                event.world.setBlockToAir(hitX, hitY, hitZ);

                return;
            }
            if (bID == Blocks.lava || bID == Blocks.flowing_lava) {
                event.setResult(Event.Result.ALLOW);
                event.result = new ItemStack(IguanaItems.clayBucketLava);
                event.world.setBlockToAir(hitX, hitY, hitZ);

                return;
            }

            Item customFilledBucket = customClayBuckets.get(bID);
            if (customFilledBucket != null) {
                event.setResult(Event.Result.ALLOW);
                event.result = new ItemStack(customFilledBucket);
                event.world.setBlockToAir(hitX, hitY, hitZ);
                return;
            }

            event.setCanceled(true);
        }
    }
}
