package net.codedstingray.blockchain.core;

import net.codedstingray.blockchain.BlockChain;
import net.codedstingray.blockchain.config.ConfigData;
import net.codedstingray.blockchain.eventhandlers.BlockChainEventListener;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Management class that is mainly responsible for managing the chainings of blocks
 */
public class BlockChainManager {

    private Logger logger = BlockChain.get().getLogger();

    private int defaultChainConfigVersion = 1;
    private HashMap<BlockChainEventListener, BlockChainData> listenerReferencedChainData = new HashMap<>();

    public void register(BlockChainEventListener listener) {
        register(listener, new BlockChainData());
    }

    public void register(BlockChainEventListener listener, BlockChainData data) {
        listenerReferencedChainData.put(listener, data);
    }

    public BlockChainData getChainDataFromListener(BlockChainEventListener listener) {
        return listenerReferencedChainData.get(listener);
    }

    public void loadChainsForListener(BlockChainEventListener listener) {
        String chainConfigName = listener.getChainConfigName();

        String chainConfigFileName = "chain_" + chainConfigName + ".conf";

        Path configDir = BlockChain.get().getConfigDir();
        Path chainConfigPath = configDir.resolve(chainConfigFileName);

        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder()
                .setPath(chainConfigPath).build();

        //get configuration from config file
        CommentedConfigurationNode rootNode = null;
        boolean chainConfigPresent = Files.exists(chainConfigPath);
        if(chainConfigPresent) {
            rootNode = loadConfig(configLoader, chainConfigPath);
        }
        //at this point, rootNode is null if
        //  a) the config file didn't exist (first time loading) or
        //  b) there was a loading error

        //generate config from default config if config not loaded and [first time loading or if we should reload faulty config]
        if (rootNode == null) {
            if (!chainConfigPresent || ConfigData.regenerateFaultyChainConfigs) {
                logger.info(chainConfigPresent
                        ? "unable to load chain config, reloading from default config;\npath: " + chainConfigPath
                        : "chain config not present, loading from default config;\npath: " + chainConfigPath
                );

                String defaultChainConfigFileName = "default_" + chainConfigFileName;
                try {
                    Sponge.getAssetManager().getAsset(BlockChain.get(), defaultChainConfigFileName).get()
                            .copyToFile(chainConfigPath, true, false);

                    rootNode = loadConfig(configLoader, chainConfigPath);
                } catch (IOException | NoSuchElementException e) {
                    logger.warn("Error on copying default chain config into config folder; unable to load config for \"" + chainConfigName + "\"");
                    e.printStackTrace();
                    return;
                }
            } else {
                logger.warn("Unable to load config for \"" + chainConfigFileName +
                        "\"; enable \"regenerate-faulty-chain-configs\" in main config to allow reloading faulty configs" +
                        " (you will lose your old configuration if you do this!)");
                return;
            }
        }


        // >>> load chains from chain config <<<
        BlockChainData blockChainData = new BlockChainData();
        int version = rootNode.getNode("version").getInt(defaultChainConfigVersion);

        //TODO: outsource this into version specific loaders
        List<? extends ConfigurationNode> chainLinks = rootNode.getNode("chainLinks").getChildrenList();

        for(ConfigurationNode chainLink: chainLinks) {
            // >>> original state <<<
            ConfigurationNode original = chainLink.getNode("original");
            BlockState originalState = parseState(original);
            if(originalState == null) {
                //TODO: logger output
                continue;
            }

            // >>> target states <<<
            List<? extends ConfigurationNode> targets = chainLink.getNode("targets").getChildrenList();
            for(ConfigurationNode target: targets) {
                //getting target state and auxiliary values
                ConfigurationNode targetNode = target.getNode("target_block");
                BlockState targetState = parseState(targetNode);
                if(targetState == null) {
                    //TODO: logger output
                    continue;
                }
                float chance = target.getNode("chance").getFloat(1);
                boolean drop = target.getNode("drop").getBoolean(false);

                //registering chain link to chainData
                blockChainData.addBlockChainValue(originalState, targetState, chance, drop);
            }
        }

        register(listener, blockChainData);
    }

    private BlockState parseState(ConfigurationNode node) {
        String originalID = node.getNode("id").getString();
        if(originalID == null) return null; //TODO: logger output

        //get default state from id
        Optional<BlockType> typeOpt = Sponge.getRegistry().getType(BlockType.class, originalID);
        if(!typeOpt.isPresent()) return null; //TODO: logger output
        BlockState defaultState = typeOpt.get().getDefaultState();

        //iterate through trait entries
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
            Object key = entry.getKey();
            ConfigurationNode value = entry.getValue();
            if ("id".equals(key) || !(key instanceof String)) continue; //TODO: logger output

            String traitKey = (String) key;
            String traitValue = value.getString();
            if (traitValue == null) continue; //TODO: logger output

            //obtaining trait
            Optional<BlockTrait<?>> traitOpt = defaultState.getTrait(traitKey);
            if (!traitOpt.isPresent()) continue; //TODO: logger output

            //adding trait to state
            Optional<BlockState> blockStateOpt = defaultState.withTrait(traitOpt.get(), traitValue);
            if (blockStateOpt.isPresent())
                defaultState = blockStateOpt.get();
            else
                ; //TODO: logger output
        }

        return defaultState;
    }

    private CommentedConfigurationNode loadConfig(ConfigurationLoader<CommentedConfigurationNode> configLoader, Path chainConfigPath) {
        CommentedConfigurationNode rootNode = null;
        try {
            rootNode = configLoader.load();
        } catch (IOException e) {
            logger.warn("Error on loading chain configuration: " + chainConfigPath);
            e.printStackTrace();
        }
        return rootNode;
    }
}
