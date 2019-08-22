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

        BlockState ironBlock = BlockState.builder().blockType(BlockTypes.IRON_BLOCK).build();
        BlockState goldBlock = BlockState.builder().blockType(BlockTypes.GOLD_BLOCK).build();
        BlockState diamondBlock = BlockState.builder().blockType(BlockTypes.DIAMOND_BLOCK).build();


        BlockChainData blockBreakData = new BlockChainData();

        blockBreakData.addBlockChainValue(stoneBrick, crackedStoneBrick, 1, false);
        blockBreakData.addBlockChainValue(chiseledStoneBrick, crackedStoneBrick, 1, false);

        blockBreakData.addBlockChainValue(mossyStoneBrick, mossyCobbleStone, 0.5f, false);
        blockBreakData.addBlockChainValue(mossyStoneBrick, crackedStoneBrick, 0.5f, false);

        blockBreakData.addBlockChainValue(stone, cobbleStone, 1, false);
        blockBreakData.addBlockChainValue(crackedStoneBrick, cobbleStone, 1, false);
        blockBreakData.addBlockChainValue(mossyCobbleStone, cobbleStone, 1, false);

        blockBreakData.addBlockChainValue(diamondBlock, goldBlock, 0.2f, true);
        blockBreakData.addBlockChainValue(diamondBlock, goldBlock, 0.8f, false);
        blockBreakData.addBlockChainValue(goldBlock, ironBlock, 1, true);

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
