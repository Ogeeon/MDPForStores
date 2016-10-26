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
//        return stock.toString() + (actions == null ? "" : actions.toString());
        return stock.toString();
    }

    @Override
    public final int compareTo(final State o) {
        if (stock.size() != o.getStock().size()) {
            throw new IllegalArgumentException("Number of item types don't match");
        }
        for (int idx = 0; idx < stock.size(); idx++) {
            if (stock.get(idx) < o.getStock().get(idx)) {
                return -1;
            }
            if (stock.get(idx) > o.getStock().get(idx)) {
                return 1;
            }
        }
        return 0;
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        State s1 = (State) o;
        if (stock.size() != s1.getStock().size()) {
            throw new IllegalArgumentException("Number of item types don't match");
        }
        for (int i = 0; i < stock.size(); i++) {
            if (stock.get(i) != s1.getStock().get(i)) {
                return false;
            }
        }
        return true;
    }
    
    public final int hashCode() {
        return stock.hashCode();
    }
}
