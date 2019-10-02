package net.codedstingray.blockchain;

import com.google.inject.Inject;
import net.codedstingray.blockchain.config.ConfigData;
import net.codedstingray.blockchain.config.ConfigManager;
import net.codedstingray.blockchain.core.BlockChainManager;
import net.codedstingray.blockchain.eventhandlers.BlockMineHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

@Plugin(
        id = "blockchain",
        name = "BlockChain",
        description = "Chain Blocks!",
        authors = {
                "CodedStingray"
        }
)
public class BlockChain {

    @Inject
    private Logger logger;
    @Inject
    private Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path mainConfigPath;
    private ConfigManager configManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private BlockChainManager blockChainManager;

    public BlockChain() {
        instance = this;
    }

    @Listener
    public void onInitialization(GameInitializationEvent event) {
        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder()
                .setPath(mainConfigPath).build();

        configManager = new ConfigManager(configLoader);
        configManager.loadConfig();


        blockChainManager = new BlockChainManager();

        if(ConfigData.enableBlockBreakListener) {
            logger.info("Block Break Listener enabled, registering listener...");

            BlockMineHandler blockMineHandler = new BlockMineHandler();
            blockChainManager.loadChainsForListener(blockMineHandler);

            Sponge.getEventManager().registerListeners(this, blockMineHandler);
        } else {
            logger.info("Block Break Listener not enabled");
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("BlockChain start");
    }


    //getters
    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public BlockChainManager getBlockChainManager() {
        return blockChainManager;
    }

    public Path getConfigDir() {
        return configDir;
    }

    //singleton
    private static BlockChain instance;

    public static BlockChain get() {
        return instance;
    }
}
