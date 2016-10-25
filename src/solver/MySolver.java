package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import problem.Store;
import problem.Matrix;
import problem.ProblemSpec;

public class MySolver implements OrderingAgent {
	
	private ProblemSpec spec = new ProblemSpec();
	private Store store;
    private List<Matrix> probabilities;
    private Set<State> states;
    private Map<State, Action> policy;
	
	public MySolver(ProblemSpec spec) throws IOException {
	    this.spec = spec;
		store = spec.getStore();
        probabilities = spec.getProbabilities();
        states = new HashSet<State>();
	}
	
	public void doOfflineComputation() {
	    states = addStates(new ArrayList<Integer>(), 0);
//	    System.out.println(states.size());
	    policy = valueIteration(1);
	}
	
	public final Map<State, Action> valueIteration(final double epsilon) {
        Map<State, Action> optimalActions = new LinkedHashMap<State, Action>();
        // Local variables: U, U', vectors of utilities for states in S, initially zero
        Map<State, Double> u = initMap(states, new Double(0));
        Map<State, Double> uDelta = initMap(states, new Double(0));
        // Maximum change in the utility of any state in an iteration
        double delta = 0;
        double minDelta = epsilon * (1 - spec.getDiscountFactor()) / spec.getDiscountFactor();
        
//        for (int iter = 0; iter < 100; iter++) {
        do {
            u.putAll(uDelta);
            delta = 0;
            // for each state s in S do
            for (State s : states) {
                // Calculating utility of a state
                Set<Action> actions = s.getActions();
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
                uDelta.put(s, getReward(s) + spec.getDiscountFactor() * aMax);
                // if |U'[s] - U[s]| > delta then delta <- |U'[s] - U[s]|
                double aDiff = Math.abs(uDelta.get(s) - u.get(s));
                if (aDiff > delta) {
                    delta = aDiff;
                }
            }
//            System.out.println(delta);
            // until delta < &epsilon(1 - gamma)/gamma
        } while (delta > minDelta);      
        return optimalActions;
    }
	
	public List<Integer> generateStockOrder(List<Integer> stockInventory,
											int numWeeksLeft) {
	    State tmp = new State(stockInventory);
	    Action act = policy.get(tmp); 
	    if (act == null) {
	        List<Integer> order = new ArrayList<Integer>();
	        for (int i = 0; i < store.getMaxTypes(); i++) {
	            order.add(0);
	        }
	        act = new Action(order);
	    }
	    
	    return act.getChange();
	}

    
    private Set<State> addStates(List<Integer> stock, int currStock) {
        Set<State> tmpStates = new HashSet<State>();
        stock.add(0);
        int tmpCurrStock = 0;
        for (int i = 0; i <= store.getCapacity() - currStock; i++) {
            List<Integer> tmpStock = new ArrayList<Integer>(stock);
            tmpStock.set(tmpStock.size() - 1, i);
            tmpCurrStock = currStock + i;
            if (tmpStock.size() < store.getMaxTypes()) {
                tmpStates.addAll(addStates(tmpStock, tmpCurrStock));
            } else {
                State s = new State(tmpStock);
                s.setActions(addActions(s, new ArrayList<Integer>(), 0, 0));
                tmpStates.add(s);
            }
        }
        return tmpStates;
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
    
    private Set<Action> addActions(State s, List<Integer> change, int currReturn, int currOrder) {
        Set<Action> tmpActions = new HashSet<Action>();
        change.add(0);
        int tmpCurrReturn = currReturn;
        int tmpCurrOrder = currOrder;
        int min = -store.getMaxReturns();
        if (currReturn < 0 && (-currReturn - min) > store.getMaxReturns()) {
            min = -(store.getMaxReturns() + currReturn);
        }
        if (s.getStock().get(change.size() - 1) + min < 0) {
            min = 0;
        }
        int max = store.getMaxPurchase();
        if (currOrder > 0) {
            max -= currOrder;
        }
        for (int order = min; order <= max; order++) {
            if (order < 0) {
                tmpCurrReturn = currReturn + order;
//                tmpCurrOrder = order;
            } else {
//                tmpCurrReturn = currReturn;
                tmpCurrOrder = currOrder + order;
            }
            List<Integer> tmpChange = new ArrayList<Integer>(change);
            tmpChange.set(tmpChange.size() - 1, order);
            if (tmpChange.size() < store.getMaxTypes()) {
                tmpActions.addAll(addActions(s, tmpChange, tmpCurrReturn, tmpCurrOrder));
            } else {
                Action a = new Action(tmpChange);
                tmpActions.add(a);
            }
        }
        return tmpActions;
    }
    
    private double getProbability(final State s, final Action a, final State s1) {
        double prob = 1;
        for (int type = 0; type < store.getMaxTypes(); type++) {
            int newStock = s.getStock().get(type) + a.getChange().get(type);
            // Can't get probablity row, if newStock exceeds storage space
            newStock = Math.min(newStock, store.getCapacity());
            int change = newStock - s1.getStock().get(type);
            if (change < 0) {
                return 0;
            }
            List<Double> row = probabilities.get(type).getRow(newStock);
            prob *= row.get(change);
            // new state has 0 stock. there is more than one way to get there 
//            if (s1.getStock().get(type) == 0) {
//                for (int want = change + 1; want <= store.getCapacity(); want++) {
//                    prob += row.get(want);
//                }
//            }
        }
        return prob;
    }

    private double getReward(final State s) {
        double r = 0;
        for (int type = 0; type < store.getMaxTypes(); type++) {
            r += s.getStock().get(type) * spec.getPrices().get(type);
        }
        return r;
    }
}
