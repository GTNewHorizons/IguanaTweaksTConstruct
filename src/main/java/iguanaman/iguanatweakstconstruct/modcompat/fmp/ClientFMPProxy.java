package iguanaman.iguanatweakstconstruct.modcompat.fmp;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

import codechicken.microblock.handler.MicroblockProxy$;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class ClientFMPProxy extends CommonFMPProxy {

    @Override
    public void updateSawRenderers() {
        Item stoneSaw = MicroblockProxy$.MODULE$.sawStone();
        Item ironSaw = MicroblockProxy$.MODULE$.sawIron();
        Item diamondSaw = MicroblockProxy$.MODULE$.sawDiamond();

        if (MicroblockProxy$.MODULE$.useSawIcons()) {
            IguanaFMPCompat.arditeSaw.setTextureName(Reference.resource("sawArdite"));
            IguanaFMPCompat.cobaltSaw.setTextureName(Reference.resource("sawCobalt"));
            IguanaFMPCompat.manyullynSaw.setTextureName(Reference.resource("sawManyullyn"));
        } else {
            MinecraftForgeClient
                    .registerItemRenderer(stoneSaw, new IguanaItemSawRenderer("microblock:textures/items/saw.png", 0));
            MinecraftForgeClient
                    .registerItemRenderer(ironSaw, new IguanaItemSawRenderer("microblock:textures/items/saw.png", 1));
            MinecraftForgeClient.registerItemRenderer(
                    diamondSaw,
                    new IguanaItemSawRenderer("microblock:textures/items/saw.png", 2));

            MinecraftForgeClient.registerItemRenderer(
                    IguanaFMPCompat.arditeSaw,
                    new IguanaItemSawRenderer(Reference.RESOURCE + ":textures/items/saw.png", 0));
            MinecraftForgeClient.registerItemRenderer(
                    IguanaFMPCompat.cobaltSaw,
                    new IguanaItemSawRenderer(Reference.RESOURCE + ":textures/items/saw.png", 1));
            MinecraftForgeClient.registerItemRenderer(
                    IguanaFMPCompat.manyullynSaw,
                    new IguanaItemSawRenderer(Reference.RESOURCE + ":textures/items/saw.png", 2));
        }
    }
}
