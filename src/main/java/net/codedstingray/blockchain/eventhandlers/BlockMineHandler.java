package net.codedstingray.blockchain.eventhandlers;

import net.codedstingray.blockchain.BlockChain;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlockMineHandler {

    private Logger logger = BlockChain.get().getLogger();

    private Set<BlockSnapshot> modifiedBlocks = new HashSet<>();

    @Listener
    public void onBlockMined(ChangeBlockEvent.Break event) {
        List<Transaction<BlockSnapshot>> brokenBlocks = event.getTransactions();

        if(!event.getCause().first(Player.class).isPresent()) {
            //if there was no player involved, don't modify teh block break
            logger.info("BlockBreakEvent not from Player, ignoring");
            return;
        }

        //loop through all transactions and edit the ones desired to edit
        for(Transaction<BlockSnapshot> transaction: brokenBlocks) {
            BlockSnapshot originalSnapshot = transaction.getOriginal();
            BlockState originalState = originalSnapshot.getState();
            BlockType originalType = originalState.getType();

            //edit transaction if desired
            if(originalType.equals(BlockTypes.STONEBRICK) || originalType.equals(BlockTypes.STONE)) {
                modifyTransaction(transaction, originalSnapshot);
            }
        }
    }

    private boolean modifyTransaction(Transaction<BlockSnapshot> transaction, BlockSnapshot originalSnapshot) {
        //get location
        Location<World> location;
        Optional<Location<World>> locationOpt = originalSnapshot.getLocation();
        if(locationOpt.isPresent()) {
            location = locationOpt.get();
        } else {
            logger.warn("Block transaction without location, ignoring this transaction; full BlockSnapshot: " + originalSnapshot);
            return false;
        }

        BlockState newState = BlockState.builder().blockType(BlockTypes.COBBLESTONE).build();
        BlockSnapshot newSnapshot = BlockSnapshot.builder().from(location).blockState(newState).build();

        transaction.setCustom(newSnapshot);

        logger.info(
                "Found block break to edit: " + originalSnapshot.getState() + " -> " + newSnapshot.getState() +
                        " at " + newSnapshot.getLocation().orElse(null)
        );
        modifiedBlocks.add(originalSnapshot);

        return true;
    }

    @Listener
    public void onItemDropped(DropItemEvent event) {
        event.getCause().first(BlockSnapshot.class).ifPresent(blockSnapshot -> {
            if(modifiedBlocks.contains(blockSnapshot)) {
                logger.info("Cause of DropItemEvent listed in modified blocks; Removing entry from Set and cancelling event");
                modifiedBlocks.remove(blockSnapshot);
                event.setCancelled(true);
            }
        });
    }
}
