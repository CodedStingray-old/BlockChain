package net.codedstingray.blockchain.core;

import org.spongepowered.api.block.BlockState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class BlockChainData {
    private static Random rng = new Random();

    private HashMap<BlockState, BlockChainValueSet> chains = new HashMap<>();

    public void addBlockChainValue(BlockState originalState, float probability, BlockState newState) {
        BlockChainValueSet chainValues = chains.get(originalState);
        if(chainValues == null)
            chains.put(originalState, (chainValues = new BlockChainValueSet()));

        chainValues.addBlockChainValue(probability, newState);
    }



    static class BlockChainValueSet {
        /**
         * List of Blockstates and their <b>accumulated</b> probabilities
         */
        LinkedList<BlockChainValue> values = new LinkedList<>();

        void addBlockChainValue(float probability, BlockState state) {
            float accumulatedProbability = values.isEmpty()
                    ? probability
                    : values.getLast().accumulatedProbability + probability;

            values.add(new BlockChainValue(accumulatedProbability, state));
        }

        BlockState getBockState() {
            float random = rng.nextFloat();

            for(BlockChainValue value: values) {
                if(random < value.accumulatedProbability)
                    return value.state;
            }

            return null;
        }
    }

    private static class BlockChainValue {
        float accumulatedProbability;
        BlockState state;

        BlockChainValue(float accumulatedProbability, BlockState state) {
            this.accumulatedProbability = accumulatedProbability;
            this.state = state;
        }
    }
}
