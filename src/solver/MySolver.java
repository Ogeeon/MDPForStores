package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import problem.Store;
import problem.Matrix;
import problem.ProblemSpec;

public class MySolver implements OrderingAgent {
	
	private ProblemSpec spec = new ProblemSpec();
	private Store store;
    private List<Matrix> probabilities;
    /** States of the process. */
    private Set<State> states;
	
	public MySolver(ProblemSpec spec) throws IOException {
	    this.spec = spec;
		store = spec.getStore();
        probabilities = spec.getProbabilities();
        states = new HashSet<State>();
	}
	
	public void doOfflineComputation() {
	    initStates();
	    for (State s: states) {
	        System.out.println(s);
	    }
	}
	
	
	public List<Integer> generateStockOrder(List<Integer> stockInventory,
											int numWeeksLeft) {
	    List<Integer> itemOrders = new ArrayList<Integer>();
	    
	    
	    return itemOrders;
	}

    
    private void initStates() {
        for (int s = 0; s <= store.getCapacity(); s++) {
            for (int r = 0; r <= store.getCapacity() - s; r++) {
                List<Integer> state = new ArrayList<Integer>();
                state.add(s);
                state.add(r);
                State tmp = new State(state);
                tmp.setActions(getActions(tmp));
                states.add(tmp);
            }
        }
    }
    
    private Set<Action> getActions(final State state) {
        // Actions that can be done are just returning or ordering items.
        Set<Action> acts = new HashSet<Action>();
        for (int a1 = -store.getMaxReturns(); a1 <= store.getMaxPurchase(); a1++) {
            int min = 0;
            if (a1 < 0) {
                if (state.getStock().get(0) + a1 < 0) {
                    continue;
                }
                if (-a1 < store.getMaxReturns()) {
                    min = -(store.getMaxReturns() + a1);
                }
            }
            int max = a1 < 0 ? store.getMaxPurchase() : store.getMaxPurchase() - a1;
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

    private double getProbability(final State s, final Action a, final State s1) {
        double prob = 1;
        for (int type = 0; type < store.getMaxTypes(); type++) {
            int newStock = s.getStock().get(type) + a.getChange().get(type);
            int change = newStock - s1.getStock().get(type);
            if (change < 0) {
                return 0;
            }
            List<Double> row = probabilities.get(type).getRow(newStock);
            prob *= row.get(change);
            // new state has 0 stock. there is more than one way to get there 
            if (s1.getStock().get(type) == 0) {
                for (int want = change + 1; want <= store.getCapacity(); want++) {
                    prob *= row.get(want);
                }
            }
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
