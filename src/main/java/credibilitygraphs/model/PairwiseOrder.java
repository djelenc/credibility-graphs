package credibilitygraphs.model;

import org.jetbrains.annotations.NotNull;

public class PairwiseOrder implements Comparable<PairwiseOrder> {
    private final double[][] comparisons;
    private final int agent;

    PairwiseOrder(int agent, double[][] comparisons) {
        this.agent = agent;
        this.comparisons = comparisons;
    }

    @Override
    public int compareTo(@NotNull PairwiseOrder that) {
        return Double.compare(comparisons[that.agent][this.agent], comparisons[this.agent][that.agent]);
    }
}
