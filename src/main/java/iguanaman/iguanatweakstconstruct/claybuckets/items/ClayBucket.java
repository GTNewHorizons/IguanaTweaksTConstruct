package iguanaman.iguanatweakstconstruct.claybuckets.items;

import net.minecraft.block.Block;

import iguanaman.iguanatweakstconstruct.reference.Reference;

public class ClayBucket extends BaseClayBucket {

    public ClayBucket(Block contents, String name, String texture) {
        super(contents, Reference.prefix(name), Reference.resource(texture));
    }

    public ClayBucket(Block contents) {
        super(contents);
    }
}
