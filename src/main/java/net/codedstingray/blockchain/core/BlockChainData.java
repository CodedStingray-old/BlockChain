package net.codedstingray.blockchain.core;

import org.spongepowered.api.block.BlockState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class BlockChainData {
    private static Random rng = new Random();

    private HashMap<BlockState, BlockChainLink> chains = new HashMap<>();

    public void addBlockChainValue(BlockState originalState, BlockState newState, float probability, boolean doDrop) {
        BlockChainLink chainValues = chains.get(originalState);
        if(chainValues == null)
            chains.put(originalState, (chainValues = new BlockChainLink()));

        chainValues.addChainLinkTarget(newState, probability, doDrop);
    }

    public ChainLinkTarget getChainLinkTarget(BlockState originalState) {
        BlockChainLink chain = chains.get(originalState);
        if(chain == null) return null;
        return chain.getChainLinkTarget();
    }

    static class BlockChainLink {
        /**
         * List of Blockstates and their <b>accumulated</b> probabilities
         */
        LinkedList<ChainLinkTarget> values = new LinkedList<>();

        void addChainLinkTarget(BlockState state, float probability, boolean doDrop) {
            float accumulatedProbability = values.isEmpty()
                    ? probability
                    : values.getLast().accumulatedProbability + probability;

            values.add(new ChainLinkTarget(state, accumulatedProbability, doDrop));
        }

        ChainLinkTarget getChainLinkTarget() {
            float random = rng.nextFloat();

            for(ChainLinkTarget value: values) {
                if(random < value.accumulatedProbability)
                    return value;
            }

            return null;
        }
    }

    public static class ChainLinkTarget {
        public final float accumulatedProbability;
        public final BlockState state;
        public final boolean doDrop;

        ChainLinkTarget(BlockState state, float accumulatedProbability, boolean doDrop) {
            this.accumulatedProbability = accumulatedProbability;
            this.state = state;
            this.doDrop = doDrop;
        }
    }
}
