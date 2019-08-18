package net.codedstingray.blockchain;

import com.google.inject.Inject;
import net.codedstingray.blockchain.core.BlockChainData;
import net.codedstingray.blockchain.core.BlockChainManager;
import net.codedstingray.blockchain.eventhandlers.BlockMineHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.BrickTypes;
import org.spongepowered.api.data.type.StoneTypes;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

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

    private BlockChainManager blockChainManager;

    public BlockChain() {
        instance = this;
    }

    @Listener
    public void onInitialization(GameInitializationEvent event) {
        blockChainManager = new BlockChainManager();

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


        BlockChainData blockBreakData = new BlockChainData();

        blockBreakData.addBlockChainValue(stoneBrick, 1, crackedStoneBrick);
        blockBreakData.addBlockChainValue(chiseledStoneBrick, 1, crackedStoneBrick);

        blockBreakData.addBlockChainValue(mossyStoneBrick, 0.5f, mossyCobbleStone);
        blockBreakData.addBlockChainValue(mossyStoneBrick, 0.5f, crackedStoneBrick);

        blockBreakData.addBlockChainValue(stone, 1, cobbleStone);
        blockBreakData.addBlockChainValue(crackedStoneBrick, 1, cobbleStone);
        blockBreakData.addBlockChainValue(mossyCobbleStone, 1, cobbleStone);

        blockChainManager.register(blockMineHandler, blockBreakData);
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
