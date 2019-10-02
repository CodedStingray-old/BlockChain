package net.codedstingray.blockchain.eventhandlers;

import net.codedstingray.blockchain.BlockChain;
import org.slf4j.Logger;

public abstract class BlockChainEventListener {

    protected Logger logger;

    private String chainConfigName;

    protected BlockChainEventListener(String chainConfigName) {
        logger = BlockChain.get().getLogger();

        this.chainConfigName = chainConfigName;
    }

    public String getChainConfigName() {
        return chainConfigName;
    }
}
