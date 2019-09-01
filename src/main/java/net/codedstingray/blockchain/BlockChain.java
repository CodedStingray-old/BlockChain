package net.codedstingray.blockchain;

import com.google.inject.Inject;
import net.codedstingray.blockchain.config.ConfigData;
import net.codedstingray.blockchain.config.ConfigManager;
import net.codedstingray.blockchain.core.BlockChainData;
import net.codedstingray.blockchain.core.BlockChainManager;
import net.codedstingray.blockchain.eventhandlers.BlockMineHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.BrickTypes;
import org.spongepowered.api.data.type.StoneTypes;
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
            Sponge.getEventManager().registerListeners(this, blockMineHandler);


            //chaindata init
            //TODO: replace with config reading
            BlockState stone = BlockState.builder().blockType(BlockTypes.STONE).add(Keys.STONE_TYPE, StoneTypes.STONE).build();

            BlockState cobbleStone = BlockState.builder().blockType(BlockTypes.COBBLESTONE).build();
            BlockState mossyCobbleStone = BlockState.builder().blockType(BlockTypes.MOSSY_COBBLESTONE).build();

            BlockState stoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.DEFAULT).build();
            BlockState crackedStoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.CRACKED).build();
            BlockState mossyStoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.MOSSY).build();
            BlockState chiseledStoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.CHISELED).build();

            BlockState ironBlock = BlockState.builder().blockType(BlockTypes.IRON_BLOCK).build();
            BlockState goldBlock = BlockState.builder().blockType(BlockTypes.GOLD_BLOCK).build();
            BlockState diamondBlock = BlockState.builder().blockType(BlockTypes.DIAMOND_BLOCK).build();


            BlockChainData blockChainDataBreak = new BlockChainData();

            blockChainDataBreak.addBlockChainValue(stoneBrick, crackedStoneBrick, 1, false);
            blockChainDataBreak.addBlockChainValue(chiseledStoneBrick, crackedStoneBrick, 1, false);

            blockChainDataBreak.addBlockChainValue(mossyStoneBrick, mossyCobbleStone, 0.5f, false);
            blockChainDataBreak.addBlockChainValue(mossyStoneBrick, crackedStoneBrick, 0.5f, false);

            blockChainDataBreak.addBlockChainValue(stone, cobbleStone, 1, false);
            blockChainDataBreak.addBlockChainValue(crackedStoneBrick, cobbleStone, 1, false);
            blockChainDataBreak.addBlockChainValue(mossyCobbleStone, cobbleStone, 1, false);

            blockChainDataBreak.addBlockChainValue(diamondBlock, goldBlock, 0.2f, true);
            blockChainDataBreak.addBlockChainValue(diamondBlock, goldBlock, 0.8f, false);
            blockChainDataBreak.addBlockChainValue(goldBlock, ironBlock, 1, true);

            blockChainManager.register(blockMineHandler, blockChainDataBreak);
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

    public BlockChainManager getBlockChainManager() {
        return blockChainManager;
    }

    //singleton
    private static BlockChain instance;

    public static BlockChain get() {
        return instance;
    }
}
