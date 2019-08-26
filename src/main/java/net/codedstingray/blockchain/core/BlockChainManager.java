package net.codedstingray.blockchain.core;

import net.codedstingray.blockchain.BlockChain;
import org.slf4j.Logger;

import java.util.HashMap;

/**
 * Management class that is mainly responsible for managing the chainings of blocks
 */
public class BlockChainManager {

    private Logger logger = BlockChain.get().getLogger();

    private HashMap<Object, BlockChainData> listenerReferencedChainData = new HashMap<>();

    public void register(Object listener) {
        register(listener, new BlockChainData());
    }

    public void register(Object listener, BlockChainData data) {
        listenerReferencedChainData.put(listener, data);
    }

    public BlockChainData getChainDataFromListener(Object listener) {
        return listenerReferencedChainData.get(listener);
    }
}
