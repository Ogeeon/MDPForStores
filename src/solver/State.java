package solver;

/**
 * Class that represents state of item type in a store.
 */
public class State implements Comparable {
    /** Amount of items in stock on Monday. */
    private int stock;
    /** Amount of items requested by customers during a week. */
    private int requested;
    
    /**
     * @param initialStock Amount of items in stock on Monday
     * @param customerWants Amount of items requested by customers during a week
     */
    public State(final int initialStock, final int customerWants) {
        stock = initialStock;
        requested = customerWants;
    }
    
    /**
     * @return Amount of items in stock on Monday
     */
    public final int getInitialStock() {
        return stock;
    }
    
    /**
     * @return Amount of items requested by customers during a week
     */
    public final int getCustomerWants() {
        return requested;
    }
    
    public String toString() {
        return "(" + stock + "," + requested + ")";
    }

    @Override
    public int compareTo(Object o) {
        int s1 = this.stock;
        int s2 = ((State) o).getInitialStock();
        int w1 = this.requested;
        int w2 = ((State) o).getCustomerWants();
        if (s2 > s1) return -1;
        if (s2 < s1) return 1;
        if (w2 > w1) return -1;
        if (w2 < w1) return 1;
        return 0;
    }
}
