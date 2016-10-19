package problem;

import java.util.HashSet;
import java.util.Set;

/**
 * Class representing Markov Decision Process.
 */
public class MDP {
    /** Max number of items the store can stock. */
    private int capacity;
    /** Max number of items the store can order in a week. */
    private int maxOrder;
    /** Max number of items the store can return in a week. */
    private int maxReturns;
    /** Fee charged for returning items. */
    private double returnFee;
    /** Price of items of a given type. */
    private double itemPrice;
    /** Transition matrix. */
    private Matrix probabilities;
    /** States of the process. */
    private Set<State> states;
    
    /**
     * @param storageSpace Max number of items the store can stock
     * @param orderSize Max number of items the store can order in a week
     * @param returnSize Max number of items the store can return in a week
     * @param fee Fee charged for returning items
     * @param price Price of items of a given type
     * @param transProbs Transition matrix
     */
    public MDP(final int storageSpace, final int orderSize, final int returnSize, 
                final double fee, final double price, final Matrix transProbs) {
        capacity = storageSpace;
        maxOrder = orderSize;
        maxReturns = returnSize;
        returnFee = fee;
        itemPrice = price;
        probabilities = transProbs;
        
        states = initStates();
        
        for (State s : states) {
            System.out.print(s + ":  ");
            Set<Action> acts = getActions(s);
            for (Action a: acts) {
                System.out.println(a.getChange() + ", ");
                for (State s1 : states) {
                    double prob = getProbability(s, a, s1);
                    if (prob > 0) {
                        System.out.print("s1=" + s1 + ": " + prob + ", " + getReward(a, s1) + "; ");
                    }
                }
                System.out.println();
            }
            
            System.out.println("");
            break;
        }
    }
    
    /**
     * @return Set of states available in the process.
     */
    private Set<State> initStates() {
        final Set<State> sts = new HashSet<State>();
        
        for (int s = 0; s <= capacity; s++) {
            for (int r = 0; r <= capacity; r++) {
                State tmp = new State(s, r);
                sts.add(tmp);
            }
        }
        
        return sts;
    }
    
    /**
     * @param state State, for actions from which we are asking. 
     * @return Set of actions available from a given state.
     */
    private Set<Action> getActions(final State state) {
        Set<Action> acts = new HashSet<Action>();
        // Actions that can be done are just returning or ordering items. 
        // A store cannot return more items that it has, and cannot exceed its capacity.
        int remainder = state.getInitialStock() - state.getCustomerWants();
        remainder = remainder < 0 ? 0 : remainder;
        int minChng = remainder - maxReturns > 0 ? -1 * maxReturns : -1 * remainder;
        int maxChng = Math.min(maxOrder, capacity - remainder);
        for (int chng = minChng; chng < maxChng; chng++) {
            Action a = new Action(chng);
            acts.add(a);
        }
        
        return acts;
    }
    
    /**
     * @param s Initial state
     * @param a Action performed by agent
     * @param s1 Target state
     * @return Probability of transition
     */
    private double getProbability(final State s, final Action a, final State s1) {
        int remainder = s.getInitialStock() - s.getCustomerWants();
        if (remainder + a.getChange() != s1.getInitialStock()) {
            return 0;
        }
        return probabilities.get(remainder + a.getChange(), s1.getCustomerWants());
    }
    
    /**
     * @param a Performed action
     * @param s1 Target state
     * @return Reward for ending up in target state after given action
     */
    private double getReward(final Action a, final State s1) {
        double r = 0;
        if (a.getChange() < 0) {
            // Store pays 50% of price of items it returns
            r = 0.5 * returnFee * a.getChange(); 
        }
        if (s1.getInitialStock() - s1.getCustomerWants() > 0) {
            r += itemPrice * s1.getCustomerWants();
        } else {
            // Store keeps 75% of price of sold items
            r += 0.75 * itemPrice * s1.getInitialStock();
            // Store is fined for 25% price of items it failed to provide
            r -= 0.25 * itemPrice * (s1.getCustomerWants() - s1.getInitialStock());
        }
            
        return r;
    }
}
