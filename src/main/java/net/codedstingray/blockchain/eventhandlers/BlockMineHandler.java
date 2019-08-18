package net.codedstingray.blockchain.eventhandlers;

import net.codedstingray.blockchain.BlockChain;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.*;

public class BlockMineHandler {

    private Logger logger = BlockChain.get().getLogger();

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
            BlockState desiredState = BlockChain.get().getBlockChainManager().getChainDataFromListener(this).getChainedState(originalState);

            BlockChain.get().getBlockChainManager().modifyTransaction(transaction, originalSnapshot, desiredState);
        }
    }
}
