package solver;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
//	    System.out.println(states);
	    if (store.getMaxTypes() < 5) {
	        policy = valueIteration(1);
	    } else {
    	    policy = initPolicy();
    	    policyIteration(policy);
//    	    System.out.println(new Date());
	    }
//	    System.out.println(new Date());
	}

    public final Map<State, Action> valueIteration(final double epsilon) {
        Map<State, Action> optimalActions = new LinkedHashMap<State, Action>();
        // Local variables: U, U', vectors of utilities for states in S, initially zero
        Map<State, Double> u = initMap(states, new Double(0));
        Map<State, Double> uDelta = initMap(states, new Double(0));
        // Maximum change in the utility of any state in an iteration
        double delta = 0;
        double minDelta = epsilon * (1 - spec.getDiscountFactor()) / spec.getDiscountFactor();

        do {
            u.putAll(uDelta);
            delta = 0;
            for (State s : states) {
                Action bestAction = new Action(new ArrayList<Integer>());
                double meu = calcMEU(s, u, bestAction);
                optimalActions.put(s, bestAction);
                uDelta.put(s, getReward(s) + spec.getDiscountFactor() * meu);
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
	    List<Integer> order;
	    if (act == null) {
	        order = new ArrayList<Integer>();
	        for (int i = 0; i < store.getMaxTypes(); i++) {
	            order.add(0);
	        }
	    } else {
	        order = new ArrayList<Integer>();
            for (int i = 0; i < store.getMaxTypes(); i++) {
                order.add(stockInventory.get(i) + act.getChange().get(i) > store.getCapacity() 
                        ? store.getCapacity() - stockInventory.get(i) 
                        : act.getChange().get(i));
            }
	    }
	    
	    return order;
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
                if (store.getMaxTypes() < 5) {
                    s.setActions(addActions(s, new ArrayList<Integer>(), 0, 0));
                } else {
                    s.setActions(addGreedyActions(s, new ArrayList<Integer>(), 0));
                }
                tmpStates.add(s);
            }
        }
        return tmpStates;
    }
    
    private Set<State> addPossibleStates(State st, List<Integer> stock) {
        Set<State> tmpStates = new HashSet<State>();
        stock.add(0);
        for (int i = st.getStock().get(stock.size() - 1); i >= 0; i--) {
            List<Integer> tmpStock = new ArrayList<Integer>(stock);
            tmpStock.set(tmpStock.size() - 1, i);
            if (tmpStock.size() < store.getMaxTypes()) {
                tmpStates.addAll(addPossibleStates(st, tmpStock));
            } else {
                State s = new State(tmpStock);
                tmpStates.add(s);
            }
        }
        return tmpStates;
    }
    
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
            } else {
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
    
    private Set<Action> addGreedyActions(State s, List<Integer> change, int currOrder) {
        Set<Action> tmpActions = new HashSet<Action>();
        change.add(0);
        int tmpCurrOrder = currOrder;
        int max = store.getMaxPurchase();
        if (currOrder > 0) {
            max -= currOrder;
        }
        for (int order = 0; order <= max; order++) {
            tmpCurrOrder = currOrder + order;
            List<Integer> tmpChange = new ArrayList<Integer>(change);
            tmpChange.set(tmpChange.size() - 1, order);
            if (tmpChange.size() < store.getMaxTypes()) {
                tmpActions.addAll(addGreedyActions(s, tmpChange, tmpCurrOrder));
            } else {
                int totalStock = 0;
                for (int i = 0; i < store.getMaxTypes(); i++) {
                    totalStock += s.getStock().get(i) + tmpChange.get(i);
                }
                if (totalStock == store.getCapacity() || tmpCurrOrder == store.getMaxPurchase()) {
                    Action a = new Action(tmpChange);
                    tmpActions.add(a);
                }
            }
        }
        return tmpActions;
    }
      
    private double getTransitionProbablity(final State s, final State s1) {
        double prob = 1;
        for (int type = 0; type < store.getMaxTypes(); type++) {
            int stock = s.getStock().get(type);
            stock = Math.min(stock, store.getCapacity());
            int change = stock - s1.getStock().get(type);
            if (change < 0) {
                return 0;
            }
            List<Double> row = probabilities.get(type).getRow(stock);
            prob *= row.get(change);
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
    
    private double calcEU(State s, Action a, Map<State, Double> u) {
        double eu = 0;
        List<Integer> stockDelta = new ArrayList<Integer>(s.getStock());
        for (int i = 0; i < a.getChange().size(); i++) {
            stockDelta.set(i, stockDelta.get(i) + a.getChange().get(i));
        }
        State s1 = new State(stockDelta);
        Set<State> targets = addPossibleStates(s1, new ArrayList<Integer>());
        for (State sDelta: targets) {
            Double util = u.get(sDelta);
            if (util == null) {
                continue;
            }
            eu += getTransitionProbablity(s1, sDelta) * util;
        }
        return eu;
    }
    
    private double calcMEU(State s, Map<State, Double> u, Action act) {
        double meu = 0;
        Set<Action> actions = s.getActions();
        for (Action a : actions) {
            double eu = calcEU(s, a, u);
            if (eu > meu) {
                meu = eu;
                act.setChange(a.getChange());
            }
        }
        return meu;
    }
    
    private Map<State, Action> initPolicy() {
        Map<State, Action> p = new LinkedHashMap<State, Action>();
        List<Integer> change = new ArrayList<Integer>();
        for (int i = 0; i < store.getMaxTypes(); i++) {
            change.add(0);
        }
        for (State s : states) {
            p.put(s, new Action(change));
        }
        return p;
    }
    
    private void policyEvaluation(Map<State, Action> p, Map<State, Double> u) {
        Map<State, Double> uDelta = initMap(states, new Double(0));
        for (State s : states) {
            double eu = calcEU(s, p.get(s), u);
            uDelta.put(s, getReward(s) + spec.getDiscountFactor() * eu);
        }
        u.putAll(uDelta);
    }

    private void policyIteration(Map<State, Action> p) {
        Map<State, Double> u = initMap(states, new Double(0));
        boolean noChange = false;
        policyEvaluation(policy, u);
        do {
            noChange = true;
//            int i = 0;
            for (State s : states) {
                Action bestAction = new Action(new ArrayList<Integer>());
                double meu = calcMEU(s, u, bestAction);
                double eu = calcEU(s, p.get(s), u);
//                System.out.println("state " + i++ + ", diff=" + (meu - eu));
                if (meu > eu) {
                    p.get(s).setChange(bestAction.getChange());
                    noChange = false;
                }
            }
        } while (!noChange);
        System.out.println("pi done");
    }
}
