package iguanaman.iguanatweakstconstruct;

import java.io.File;

import mantle.pulsar.config.IConfiguration;
import mantle.pulsar.pulse.PulseMeta;

import net.minecraftforge.common.config.Configuration;

/** Reimplementation of mantle.pulsar.config because static config */
public class PulsarCFG implements IConfiguration {

    private static Configuration config;
    private final File confFile;
    private final String description;

    /**
     * Creates a new Configuration object.
     *
     * Do NOT make this the same as the overall mod configuration; it will clobber it!
     *
     * @param confFile    The config file
     * @param description The description for the group that the config entries will be placed in.
     */
    public PulsarCFG(File confFile, String description) {
        this.confFile = confFile;
        this.description = description;
    }

    @Override
    public void load() {
        config = new Configuration(confFile);
        config.load();
    }

    @Override
    public boolean isModuleEnabled(PulseMeta meta) {
        return config.get(description, meta.getId(), meta.isEnabled(), meta.getDescription())
                .getBoolean(meta.isEnabled());
    }

    @Override
    public void flush() {
        if (config.hasChanged()) config.save();
    }
}
