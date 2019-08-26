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

        chainValues.addBlockChainValue(newState, probability, doDrop);
    }

    public BlockChainValue getChainedState(BlockState originalState) {
        BlockChainLink chain = chains.get(originalState);
        if(chain == null) return null;
        return chain.getChainValue();
    }

    static class BlockChainLink {
        /**
         * List of Blockstates and their <b>accumulated</b> probabilities
         */
        LinkedList<BlockChainValue> values = new LinkedList<>();

        void addBlockChainValue(BlockState state, float probability, boolean doDrop) {
            float accumulatedProbability = values.isEmpty()
                    ? probability
                    : values.getLast().accumulatedProbability + probability;

            values.add(new BlockChainValue(state, accumulatedProbability, doDrop));
        }

        @Deprecated
        BlockState getChainedState() {
            float random = rng.nextFloat();

            for(BlockChainValue value: values) {
                if(random < value.accumulatedProbability)
                    return value.state;
            }

            return null;
        }

        BlockChainValue getChainValue() {
            float random = rng.nextFloat();

            for(BlockChainValue value: values) {
                if(random < value.accumulatedProbability)
                    return value;
            }

            return null;
        }
    }

    public static class BlockChainValue {
        public final float accumulatedProbability;
        public final BlockState state;
        public final boolean doDrop;

        BlockChainValue(BlockState state, float accumulatedProbability, boolean doDrop) {
            this.accumulatedProbability = accumulatedProbability;
            this.state = state;
            this.doDrop = doDrop;
        }
    }
}
