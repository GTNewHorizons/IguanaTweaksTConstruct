package iguanaman.iguanatweakstconstruct.claybuckets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import biomesoplenty.api.content.BOPCBlocks;
import buildcraft.BuildCraftEnergy;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.railcraft.common.fluids.RailcraftFluids;
import tconstruct.smeltery.TinkerSmeltery;

public class ClayBucketHandler {

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
            if (bID == RailcraftFluids.CREOSOTE.getBlock()) {
                event.setResult(Event.Result.ALLOW);
                event.result = new ItemStack(IguanaItems.clayBucketCreosote);
                event.world.setBlockToAir(hitX, hitY, hitZ);

                return;
            }
            if (bID == BuildCraftEnergy.blockOil) {
                event.setResult(Event.Result.ALLOW);
                event.result = new ItemStack(IguanaItems.clayBucketOil);
                event.world.setBlockToAir(hitX, hitY, hitZ);

                return;
            }
            if (bID == BOPCBlocks.poison) {
                event.setResult(Event.Result.ALLOW);
                event.result = new ItemStack(IguanaItems.clayBucketPoison);
                event.world.setBlockToAir(hitX, hitY, hitZ);

                return;
            }
            if (bID == BOPCBlocks.blood) {
                event.setResult(Event.Result.ALLOW);
                event.result = new ItemStack(IguanaItems.clayBucketBlood);
                event.world.setBlockToAir(hitX, hitY, hitZ);

                return;
            }

            event.setCanceled(true);
        }
    }
}
