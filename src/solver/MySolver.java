package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import problem.Store;
import problem.Matrix;
import problem.ProblemSpec;

public class MySolver implements OrderingAgent {
	
	private ProblemSpec spec = new ProblemSpec();
	private Store store;
    private List<Matrix> probabilities;
    private List<ItemType> types;
	
	public MySolver(ProblemSpec spec) throws IOException {
	    this.spec = spec;
		store = spec.getStore();
        probabilities = spec.getProbabilities();
	}
	
	public void doOfflineComputation() {
	    types = new ArrayList<ItemType>();
	    List<Double> p = spec.getPrices();
	    for (int id = 0; id < p.size(); id++) {
	        types.add(new ItemType(id, p.get(id)));
	    }
	    
	    types.sort(new Comparator<ItemType>() {
            @Override
            public int compare(ItemType arg0, ItemType arg1) {
                if (arg1.getPrice() > arg0.getPrice()) {
                    return 1;
                } else if (arg1.getPrice() < arg0.getPrice()) {
                    return -1;
                }
                return 0;
            }
	    });
	    List<Integer> storageParts = getStorageParts(store.getCapacity(), store.getMaxTypes());
	    for (int idx = 0; idx < types.size(); idx++) {
	        types.get(idx).setStorage(storageParts.get(idx));
	    }
	    
	        MDP itemP = new MDP(store.getCapacity(),store.getMaxPurchase(), store.getMaxReturns(),
	                spec.getPenaltyFee(), spec.getPrices().get(0), 
	                spec.getDiscountFactor(), probabilities.get(0));
//	        t.setPolicy(itemP.valueIteration(1));
	}
	
	
	public List<Integer> generateStockOrder(List<Integer> stockInventory,
											int numWeeksLeft) {
	    List<Integer> itemOrders = new ArrayList<Integer>();

	    int ordered = 0;
	    int totalOrdered = 0;
	    int curr = 0;
	    int totalStock = 0;
	    for (int itemId = 0; itemId < stockInventory.size(); itemId++) {
	        curr = stockInventory.get(itemId);
	        totalStock += curr;
	        for (ItemType t: types) {
	            if (t.getId() == itemId) {
	                if (curr < t.getPolicy().size()) {
    	                ordered = t.getPolicy().get(curr);
    	                if (curr + ordered > t.getStorage()) {
    	                    ordered -= t.getStorage() - (curr + ordered);
    	                }
	                }
	                break;
	            }
	        }
	        itemOrders.add(itemId, ordered);
	        totalOrdered += ordered;
	    }
	    int idx = types.size() - 1;
	    int id = 0;
        // pre-cut to not exceed maximum order size
        while (totalOrdered > store.getMaxPurchase()) {
            id = types.get(idx).getId();
            if (itemOrders.get(id) > 0) {
                itemOrders.set(id, itemOrders.get(id) - 1);
                totalOrdered--;
            }
            idx--;
            if (idx < 0) {
                idx = types.size() - 1;
            }
        }
	    
	    return itemOrders;
	}

	private List<Integer> getStorageParts(int totalStorage, int typesCount) {
	    List<Integer> parts = new ArrayList<Integer>();
	    double d = 1.0 / totalStorage;
	    double r = 1.0 * typesCount / totalStorage;
	    int a = (int) Math.ceil(1.0 * totalStorage / typesCount);
	    int b = (int) Math.floor(1.0 * totalStorage / typesCount);
	    for (int idx = 1; idx <= typesCount; idx++) {
	        parts.add((idx * d < r) ? a : b);
	    }
	    return parts;
	}
}
