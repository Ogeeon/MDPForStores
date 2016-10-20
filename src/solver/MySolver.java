package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import problem.Store;
import problem.Matrix;
import problem.ProblemSpec;

public class MySolver implements OrderingAgent {
	
	private ProblemSpec spec = new ProblemSpec();
	private Store store;
    private List<Matrix> probabilities;
	
	public MySolver(ProblemSpec spec) throws IOException {
	    this.spec = spec;
		store = spec.getStore();
        probabilities = spec.getProbabilities();
	}
	
	public void doOfflineComputation() {
	    MDP itemP = new MDP(store.getCapacity(), store.getMaxPurchase(), store.getMaxReturns(),
	            spec.getPenaltyFee(), spec.getPrices().get(0), spec.getDiscountFactor(), probabilities.get(0));
	    Map<State, Action> policy = new LinkedHashMap<State, Action>();
	    Map<State, Double> utils = itemP.performValueIteration(1, policy);
	    
	}
	
	
	public List<Integer> generateStockOrder(List<Integer> stockInventory,
											int numWeeksLeft) {

		List<Integer> itemOrders = new ArrayList<Integer>();
		List<Integer> itemReturns = new ArrayList<Integer>();

		// Example code that buys one of each item type.
        // TODO Replace this with your own code.

		int totalItems = 0;
		for (int i : stockInventory) {
			totalItems += i;
		}
		
		int totalOrder = 0;
		for (int i = 0; i < store.getMaxTypes(); i++) {
			if (totalItems >= store.getCapacity() ||
			        totalOrder >= store.getMaxPurchase()) {
				itemOrders.add(0);
			} else {
				itemOrders.add(1);
				totalOrder ++;
				totalItems ++;
			}
			itemReturns.add(0);
		}


		// combine orders and returns to get change for each item type
		List<Integer> order = new ArrayList<Integer>(itemOrders.size());
		for(int i = 0; i < itemOrders.size(); i++) {
			order.add(itemOrders.get(i) - itemReturns.get(i));
		}

		return order;
	}

}
