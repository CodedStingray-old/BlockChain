package net.codedstingray.blockchain.core;

import net.codedstingray.blockchain.BlockChain;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Management class that is mainly responsible for managing the chainings of blocks
 */
public class BlockChainManager {

    private Logger logger = BlockChain.get().getLogger();

    /**
     * The set of recently modified blocks; This set will be emptied every tick
     */
    private Set<BlockSnapshot> modifiedBlocks = new HashSet<>();

    private HashMap<Object, BlockChainData> listenerReferencedChainData = new HashMap<>();

    public void register(Object listener) {
        register(listener, new BlockChainData());
    }

    public void register(Object listener, BlockChainData data) {
        listenerReferencedChainData.put(listener, data);
    }

    public boolean modifyTransaction(Transaction<BlockSnapshot> transaction, BlockSnapshot originalSnapshot, BlockState desiredState) {
        if(desiredState == null) return false; //we don't wanna modify this block

        //get location
        Location<World> location;
        Optional<Location<World>> locationOpt = originalSnapshot.getLocation();
        if(locationOpt.isPresent()) {
            location = locationOpt.get();
        } else {
            logger.warn("Block transaction without location, ignoring this transaction; full BlockSnapshot: " + originalSnapshot);
            return false;
        }

        BlockSnapshot newSnapshot = BlockSnapshot.builder().from(location).blockState(desiredState).build();

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
                event.setCancelled(true);
            }
        });
    }


}
