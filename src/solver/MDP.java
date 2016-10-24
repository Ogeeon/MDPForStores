package solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    /** Part of storage space reserved for this item type. */
    private int stockPart;
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
        for (State s: states) {
            System.out.println(s);
            Set<Action> actions = getActions(s);
            System.out.println(actions);
        }
        
    }
    
    /**
     * @return Set of states available in the process.
     */
    private Set<State> initStates() {
        final Set<State> sts = new HashSet<State>();
        for (int s = 0; s <= capacity; s++) {
            for (int r = 0; r <= capacity - s; r++) {
                List<Integer> state = new ArrayList<Integer>();
                state.add(s);
                state.add(r);
                State tmp = new State(state);
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
        // Actions that can be done are just returning or ordering items.
        Set<Action> acts = new HashSet<Action>();
        for (int a1 = -maxReturns; a1 <= maxOrder; a1++) {
            int min = 0;
            if (a1 < 0) {
                if (state.getStock().get(0) + a1 < 0) {
                    continue;
                }
                if (-a1 < maxReturns) {
                    min = -(maxReturns + a1);
                }
            }
            int max = a1 < 0 ? maxOrder : maxOrder - a1;
            for (int a2 = min; a2 <= max; a2++) {
                if (a2 < 0 && state.getStock().get(1) + a2 < 0) {
                    continue;
                }
                List<Integer> act = new ArrayList<Integer>();
                act.add(a1);
                act.add(a2);
                acts.add(new Action(act));
            }
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
        /*
        int remainder = s.getInitialStock() - s.getCustomerWants();
        remainder = remainder < 0 ? 0 : remainder;
        if (remainder + a.getChange() != s1.getInitialStock()) {
            return 0;
        }
        return probabilities.get(remainder + a.getChange(), s1.getCustomerWants());
        */
        return 0;
    }
    
    /**
     * @param a Performed action
     * @param s1 Target state
     * @return Reward for ending up in target state after given action
     */
    private double getReward(final Action a, final State s1) {
        double r = 0;
        /*
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
            */
        return r;
    }

    /**
     * The value iteration algorithm for calculating the utility of states.
     * @param epsilon The maximum error allowed in the utility of any state
     * @return Mapping of stock amounts to recommended actions
     */
    public final Map<Integer, Integer> valueIteration(final double epsilon) {
        /*
        Map<State, Action> optimalActions = new LinkedHashMap<State, Action>();
        // Local variables: U, U', vectors of utilities for states in S, initially zero
        Map<State, Double> u = initMap(states, new Double(0));
        Map<State, Double> uDelta = initMap(states, new Double(0));
        // Maximum change in the utility of any state in an iteration
        double delta = 0;
        double minDelta = epsilon * (1 - gamma) / gamma;
        Action noAct = new Action(0);
        
        do {
            u.putAll(uDelta);
            delta = 0;
            // for each state s in S do
            for (State s : states) {
                // Calculating utility of a state
                Set<Action> actions = getActions(s);
                double aMax = 0;
                for (Action a : actions) {
                    double aSum = 0;
                    for (State sDelta : states) {
                        aSum += getProbability(s, a, sDelta) * u.get(sDelta);
                    }
                    if (aSum > aMax) {
                        aMax = aSum;
                        optimalActions.put(s, a);
                    }
                }
                uDelta.put(s, getReward(noAct, s) + gamma * aMax);
                // if |U'[s] - U[s]| > delta then delta <- |U'[s] - U[s]|
                double aDiff = Math.abs(uDelta.get(s) - u.get(s));
                if (aDiff > delta) {
                    delta = aDiff;
                }
            }
            // until delta < &epsilon(1 - gamma)/gamma
        } while (delta > minDelta);
        
        Map<State, Action> sortedPolicy = new TreeMap<State, Action>(optimalActions);
        Map<Integer, Integer> policy = new HashMap<Integer, Integer>();
        /*
        for (Entry<State, Action> entry : sortedPolicy.entrySet()) {
            State s = (State) entry.getKey(); 
            if (s.getCustomerWants() == 0) {
                policy.put(s.getInitialStock(), ((Action) entry.getValue()).getChange());
            }
        }
        
        return policy;
        */
        return new LinkedHashMap<Integer, Integer>(); 
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
}
