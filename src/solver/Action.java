package solver;

import java.util.List;

/**
 * Class representing agent's action - i.e. ordering or returning items.
 */
public class Action {
    /** Change in stock - amount ordered or returned. */
    private List<Integer> change;
    
    /**
     * @param stockChange Change in stock - amount ordered or returned
     */
    public Action(final List<Integer> stockChange) {
        change = stockChange;
    }
    
    /**
     * @return Change in stock - amount ordered or returned
     */
    public final List<Integer> getChange() {
        return change;
    }
    
    /**
     * @param newChange new list of values for order
     */
    public void setChange(List<Integer> newChange) {
        change = newChange;
    }
    
    public final String toString() {
        return String.valueOf(change);
    }
}
