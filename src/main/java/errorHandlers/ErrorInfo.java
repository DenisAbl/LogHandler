package errorHandlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ErrorInfo {

    private String name;
    private volatile AtomicInteger totalQuantity;
    private volatile ConcurrentHashMap<String,AtomicInteger> quantityPerHour;

    public ErrorInfo(String name, String hour) {

        synchronized (this){
        this.name = name;
        totalQuantity = new AtomicInteger(1);
        quantityPerHour = new ConcurrentHashMap<>();
        quantityPerHour.put(hour,new AtomicInteger(1));}
    }

    public AtomicInteger getTotalQuantity() {
        return totalQuantity;
    }

    public void incrementTotalQuantity() {
        totalQuantity.incrementAndGet();
    }

    public int getQuantityPerHour(String name) {
        return quantityPerHour.get(name).get();
    }

    public void setQuantityPerHour(String name, int value) {
        quantityPerHour.get(name).set(value);
    }

    public void incrementQuantityPerHour(String hour){
        if (quantityPerHour.get(hour)!= null) {
            quantityPerHour.get(hour).incrementAndGet();
        }
        else quantityPerHour.put(hour,new AtomicInteger(1));
    }

    public Map<String,AtomicInteger> getQuantityPerHour(){
        return quantityPerHour;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
