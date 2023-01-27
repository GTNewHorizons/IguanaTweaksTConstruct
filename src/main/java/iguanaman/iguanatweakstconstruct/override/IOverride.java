package iguanaman.iguanatweakstconstruct.override;

import net.minecraftforge.common.config.Configuration;

public interface IOverride {
    void createDefault(Configuration config);

    void processConfig(Configuration config);
}
