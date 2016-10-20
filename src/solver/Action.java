package solver;

/**
 * Class representing agent's action - i.e. ordering or returning items.
 */
public class Action {
    /** Change in stock - amount ordered or returned. */
    private int change = 0;
    
    /**
     * @param stockChange Change in stock - amount ordered or returned
     */
    public Action(final int stockChange) {
        change = stockChange;
    }
    
    /**
     * @return Change in stock - amount ordered or returned
     */
    public final int getChange() {
        return change;
    }
    
    public final String toString() {
        return String.valueOf(change);
    }
}
