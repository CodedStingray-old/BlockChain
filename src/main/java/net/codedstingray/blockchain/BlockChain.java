package net.codedstingray.blockchain;

import com.google.inject.Inject;
import net.codedstingray.blockchain.eventhandlers.BlockMineHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
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

    public BlockChain() {
        instance = this;
    }

    @Listener
    public void onInitialization(GameInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new BlockMineHandler());
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("BlockChain start");
    }


    public Logger getLogger() {
        return logger;
    }

    //singleton
    private static BlockChain instance;

    public static BlockChain get() {
        return instance;
    }
}
