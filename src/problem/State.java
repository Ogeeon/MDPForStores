package problem;

/**
 * Class that represents state of item type in a store.
 */
public class State {
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
}
