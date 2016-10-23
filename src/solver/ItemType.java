package solver;

import java.util.Map;
import java.util.Map.Entry;

public class ItemType{
    private int id;
    private double price;
    private int storage;
    private Map<Integer, Integer> policy;
    
    public ItemType(int typeId, double itemTypePrice) {
        id = typeId;
        price = itemTypePrice;
    }
    
    public int getId() {
        return id;
    }
    
    public double getPrice() {
        return price;
    }
    
    public int getStorage(){
        return storage;
    }
    
    public void setStorage(int storagePart) {
        storage = storagePart;
    }
    
    public Map<Integer, Integer> getPolicy () {
        return policy;
    }
    
    public void setPolicy(Map<Integer, Integer> orderingPolicy) {
        policy = orderingPolicy;
    }
    
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(":[price=");
        sb.append(price);
        sb.append(", storage=");
        sb.append(storage);
        if (policy != null) {
            sb.append(", policy=(");
            boolean first = true;
            for (Entry<Integer, Integer> e: policy.entrySet()) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append(e.getKey());
                sb.append("->");
                sb.append(e.getValue());
            }
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
