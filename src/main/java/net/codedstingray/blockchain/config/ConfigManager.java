package net.codedstingray.blockchain.config;

import net.codedstingray.blockchain.BlockChain;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class ConfigManager {

    private Logger logger = BlockChain.get().getLogger();

    private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private Path configPath;

    private ConfigurationNode configRootNode = null;

    public ConfigManager(ConfigurationLoader<CommentedConfigurationNode> configLoader) {
        this.configLoader = configLoader;
    }

    public void loadConfig() {
        //get default config
        URL defaultConfig = Sponge.getAssetManager().getAsset(BlockChain.get(), "default_blockchain.conf").get().getUrl();
        ConfigurationNode defaultConfigRootNode = null;
        try {
            defaultConfigRootNode = HoconConfigurationLoader.builder()
                    .setURL(defaultConfig)
                    .build()
                    .load();
        } catch (IOException e) {
            logger.warn("Error on loading default configuration");
            e.printStackTrace();
        }

        //get configuration from config file
        try {
            configRootNode = configLoader.load();
        } catch (IOException e) {
            logger.warn("Error on loading configuration");
            e.printStackTrace();
            configRootNode = configLoader.createEmptyNode();
        }

        //merge default config into loaded config
        if(defaultConfigRootNode != null) {
            configRootNode.mergeValuesFrom(defaultConfigRootNode);
        } else {
            logger.warn("Default config not loaded, unable to merge into loaded config");
        }

        try {
            configLoader.save(configRootNode);
        } catch (IOException e) {
            logger.warn("IOException on saving config");
        }


        ConfigData.regenerateFaultyChainConfigs = configRootNode.getNode("regenerate-faulty-chain-configs").getBoolean(false);

        ConfigurationNode listeners = configRootNode.getNode("listeners");
        ConfigData.enableBlockBreakListener = listeners.getNode("enable-block-break-listener").getBoolean(false);
        ConfigData.enableRandomTickListener = listeners.getNode("enable-random-tick-listener").getBoolean(false);
    }
}
