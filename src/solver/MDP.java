package solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import problem.Matrix;

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
    /** Discount factor. */
    private double gamma;
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
     * @param discount Process disctount factor
     * @param transProbs Transition matrix
     */
    public MDP(final int storageSpace, final int orderSize, final int returnSize, 
                final double fee, final double price, final double discount, final Matrix transProbs) {
        capacity = storageSpace;
        maxOrder = orderSize;
        maxReturns = returnSize;
        returnFee = fee;
        itemPrice = price;
        gamma = discount;
        probabilities = transProbs;
        
        states = initStates();
        
        /*
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
        }
        */
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
        int minChng = -Math.min(remainder, maxReturns);
        int maxChng = Math.min(maxOrder, capacity - remainder);
        for (int chng = minChng; chng <= maxChng; chng++) {
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
        remainder = remainder < 0 ? 0 : remainder;
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
            // Store keeps 75% of price of sold items
            r += 0.75 * itemPrice * s1.getCustomerWants();
        } else {
            // Store keeps 75% of price of sold items
            r += 0.75 * itemPrice * s1.getInitialStock();
            // Store is fined for 25% price of items it failed to provide
            r -= 0.25 * itemPrice * (s1.getCustomerWants() - s1.getInitialStock());
        }
            
        return r;
    }

    /**
     * The value iteration algorithm for calculating the utility of states.
     * @param epsilon The maximum error allowed in the utility of any state
     * @param optimalActions Map with optimal policy
     * @return a vector of utilities for states in S
     */
    public final Map<State, Double> performValueIteration(final double epsilon, 
                                                          final Map<State, Action> optimalActions) {
        // Local variables: U, U', vectors of utilities for states in S, initially zero
        Map<State, Double> u = initMap(states, new Double(0));
        Map<State, Double> uDelta = initMap(states, new Double(0));
        // Maximum change in the utility of any state in an iteration
        double delta = 0;
        // Note: Just calculate this once for efficiency purposes:
        // &epsilon;(1 - &gamma;)/&gamma;
        double minDelta = epsilon * (1 - gamma) / gamma;
        Action noAct = new Action(0);
        
        for (int iter = 0; iter < 100; iter++) {
        // repeat
//        do {
            // U <- U'; &delta; <- 0
            u.putAll(uDelta);
            delta = 0;
            // for each state s in S do
            for (State s : states) {
                // max<sub>a &isin; A(s)</sub>
                Set<Action> actions = getActions(s);
                double aMax = 0;
                for (Action a : actions) {
                    // &Sigma;<sub>s'</sub>P(s' | s, a) U[s']
                    double aSum = 0;
                    for (State sDelta : states) {
                        aSum += getProbability(s, a, sDelta) * u.get(sDelta);
                    }
                    if (aSum > aMax) {
                        // aSum > aMax) {
                        aMax = aSum;
                        optimalActions.put(s, a);
                    }
                }
                // U'[s] <- R(s) + &gamma;
                // max<sub>a &isin; A(s)</sub>
                // TODO shouldn't use noAction
                uDelta.put(s, getReward(noAct, s) + gamma * aMax);
                // if |U'[s] - U[s]| > &delta; then &delta; <- |U'[s] - U[s]|
                double aDiff = Math.abs(uDelta.get(s) - u.get(s));
                if (aDiff > delta) {
                    delta = aDiff;
                }
            }
            System.out.println(delta);
            // until &delta; < &epsilon;(1 - &gamma;)/&gamma;
//        } while (delta > minDelta);
        }
        printUtils(uDelta);
        return u;
    }

    /**
     * @param keys Map keys
     * @param value Value for entries
     * @return Map with given keys, populated with given value
     */
    private <K, V> Map<K, V> initMap(Collection<K> keys, V value) {
        Map<K, V> map = new LinkedHashMap<K, V>();
        for (K k : keys) {
            map.put(k, value);
        }
        return map;
    }
    
    @SuppressWarnings("unchecked")
    private void printUtils(Map<State, Double> utils) {
        Map<State, Double> treeMap = new TreeMap<State, Double>(utils);
        for (Entry<State, Double> entry : treeMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
