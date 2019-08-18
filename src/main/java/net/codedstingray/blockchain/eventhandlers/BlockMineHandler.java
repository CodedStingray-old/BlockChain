package net.codedstingray.blockchain.eventhandlers;

import net.codedstingray.blockchain.BlockChain;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.BrickTypes;
import org.spongepowered.api.data.type.StoneTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.*;

public class BlockMineHandler {

    private Logger logger = BlockChain.get().getLogger();

    private Map<BlockState, BlockState> blockStateMap = new HashMap<>();

    public BlockMineHandler() {
        BlockState stone = BlockState.builder().blockType(BlockTypes.STONE).add(Keys.STONE_TYPE, StoneTypes.STONE).build();

        BlockState cobbleStone = BlockState.builder().blockType(BlockTypes.COBBLESTONE).build();
        BlockState mossyCobbleStone = BlockState.builder().blockType(BlockTypes.MOSSY_COBBLESTONE).build();

        BlockState stoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.DEFAULT).build();
        BlockState crackedStoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.CRACKED).build();
        BlockState mossyStoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.MOSSY).build();
        BlockState chiseledStoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.CHISELED).build();


        blockStateMap.put(stoneBrick, crackedStoneBrick);
        blockStateMap.put(chiseledStoneBrick, crackedStoneBrick);

        blockStateMap.put(mossyStoneBrick, mossyCobbleStone);

        blockStateMap.put(stone, cobbleStone);
        blockStateMap.put(crackedStoneBrick, cobbleStone);
        blockStateMap.put(mossyCobbleStone, cobbleStone);
    }

    @Listener
    public void onBlockMined(ChangeBlockEvent.Break event) {
        if(!event.getCause().first(Player.class).isPresent()) {
            //if there was no player involved, don't modify the block break
            logger.info("BlockBreakEvent not from Player, ignoring");
            return;
        }

        List<Transaction<BlockSnapshot>> brokenBlocks = event.getTransactions();

        //loop through all transactions and edit the ones desired to edit
        for(Transaction<BlockSnapshot> transaction: brokenBlocks) {
            BlockSnapshot originalSnapshot = transaction.getOriginal();
            BlockState originalState = originalSnapshot.getState();

            //edit transaction if desired
            BlockState desiredState = blockStateMap.get(originalState);

            BlockChain.get().getBlockChainManager().modifyTransaction(transaction, originalSnapshot, desiredState);
        }
    }
}
