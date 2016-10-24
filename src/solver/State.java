package solver;

import java.util.List;
import java.util.Set;

/**
 * Class that represents state of item type in a store.
 */
public class State implements Comparable<State> {
    /** Amount of items in stock on Monday. */
    private List<Integer> stock;
    private Set<Action> actions;
    
    /**
     * @param stateStock Amount of items in stock on Monday
     */
    public State(List<Integer> stateStock) {
        stock = stateStock;
    }
    
    /**
     * @return Amount of items in stock on Monday
     */
    public final List<Integer> getStock() {
        return stock;
    }
    
    public void setActions(Set<Action> stateActions) {
        actions = stateActions;
    }
    
    public Set<Action> getActions() {
        return actions;
    }
    
    
    public String toString() {
        return stock.toString() + actions.toString();
    }

    @Override
    public int compareTo(State o) {
        if (stock.size() != o.getStock().size()) {
            throw new IllegalArgumentException("Number of item types don't match");
        }
        for (int idx = 0; idx < stock.size(); idx++) {
            if (stock.get(idx) < o.getStock().get(idx)) {
                return 1;
            }
            if (stock.get(idx) > o.getStock().get(idx)) {
                return -1;
            }
        }
        return 0;
    }
}
