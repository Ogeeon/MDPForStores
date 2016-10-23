package solver;

import java.util.List;

/**
 * Class that represents state of item type in a store.
 */
public class State implements Comparable<State> {
    /** Amount of items in stock on Monday. */
    private List<Integer> stock;
    
    /**
     * @param initialStock Amount of items in stock on Monday
     */
    public State(List<Integer> initialStock) {
        stock = initialStock;
    }
    
    /**
     * @return Amount of items in stock on Monday
     */
    public final List<Integer> getInitialStock() {
        return stock;
    }
    
    public String toString() {
        return stock.toString();
    }

    @Override
    public int compareTo(State o) {
        if (stock.size() != o.getInitialStock().size()) {
            throw new IllegalArgumentException("Number of item types don't match");
        }
        for (int idx = 0; idx < stock.size(); idx++) {
            if (stock.get(idx) < o.getInitialStock().get(idx)) {
                return 1;
            }
            if (stock.get(idx) > o.getInitialStock().get(idx)) {
                return -1;
            }
        }
        return 0;
    }
}
