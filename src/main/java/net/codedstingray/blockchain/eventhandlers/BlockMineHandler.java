package net.codedstingray.blockchain.eventhandlers;

import net.codedstingray.blockchain.BlockChain;
import net.codedstingray.blockchain.core.BlockChainData;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.scheduler.Task;

import java.util.*;

public class BlockMineHandler extends BlockChainEventListener {

    public BlockMineHandler() {
        super("blockbreak");
    }

    @Listener
    public void onBlockMined(ChangeBlockEvent.Break event) {
        if(!event.getCause().first(Player.class).isPresent()) {
            //if there was no player involved, don't modify the block break
            logger.info("BlockBreakEvent not from Player, ignoring");
            return;
        }

        List<Transaction<BlockSnapshot>> brokenBlocks = event.getTransactions();

        List<Transaction<BlockSnapshot>> modifiedTransactions = new LinkedList<>();

        for(Transaction<BlockSnapshot> transaction: brokenBlocks) {
            if(!transaction.isValid()) continue;

            BlockSnapshot originalSnapshot = transaction.getOriginal();
            BlockState originalState = originalSnapshot.getState();

            //split transactions into those transactions to edit and those to leave as they are
            BlockChainData.ChainLinkTarget chainValue = BlockChain.get().getBlockChainManager().getChainDataFromListener(this).getChainLinkTarget(originalState);

            if(chainValue != null) {
                BlockState desiredState = chainValue.state;
                BlockSnapshot desiredSnapshot = BlockSnapshot.builder().from(originalSnapshot).blockState(desiredState).build();
                modifiedTransactions.add(new Transaction<>(originalSnapshot, desiredSnapshot));
                transaction.setValid(chainValue.doDrop); //invalidate transaction if we don't wanna drop
            }
        }

        if(!modifiedTransactions.isEmpty()) {
            for (Transaction<BlockSnapshot> transaction : modifiedTransactions) {
                transaction.getOriginal().getLocation().ifPresent(location -> {
                    Task task = Task.builder().delayTicks(1).execute(taskInternal -> {
                        location.setBlock(transaction.getFinal().getState());
                        taskInternal.cancel();
                    }).submit(BlockChain.get());
                });
            }
        }
    }
}
