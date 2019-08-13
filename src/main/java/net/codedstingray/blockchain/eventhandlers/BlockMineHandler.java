package net.codedstingray.blockchain.eventhandlers;

import net.codedstingray.blockchain.BlockChain;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableBrickData;
import org.spongepowered.api.data.type.BrickType;
import org.spongepowered.api.data.type.BrickTypes;
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
            BlockState desiredState = null;

            //TODO: replace with table that holds which states should be changed to which states
            BlockState cobbleStone = BlockState.builder().blockType(BlockTypes.COBBLESTONE).build();
            BlockState mossyCobbleStone = BlockState.builder().blockType(BlockTypes.MOSSY_COBBLESTONE).build();
            BlockState crackedStoneBrick = BlockState.builder().blockType(BlockTypes.STONEBRICK).add(Keys.BRICK_TYPE, BrickTypes.CRACKED).build();

            Optional<BrickType> brickTypeOpt = originalState.get(Keys.BRICK_TYPE);
            if(originalType.equals(BlockTypes.STONEBRICK) && brickTypeOpt.isPresent()) {
                BrickType brickType = brickTypeOpt.get();

                if(brickType == BrickTypes.DEFAULT || brickType == BrickTypes.CHISELED)
                    desiredState = crackedStoneBrick;
                else if(brickType == BrickTypes.CRACKED)
                    desiredState = cobbleStone;
                else if(brickType == BrickTypes.MOSSY)
                    desiredState = mossyCobbleStone;

            } else if(originalType.equals(BlockTypes.STONE)) {
                desiredState = cobbleStone;
            }

            modifyTransaction(transaction, originalSnapshot, desiredState);
        }
    }

    private boolean modifyTransaction(Transaction<BlockSnapshot> transaction, BlockSnapshot originalSnapshot, BlockState desiredState) {
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
                modifiedBlocks.remove(blockSnapshot);
                event.setCancelled(true);
            }
        });
    }
}
