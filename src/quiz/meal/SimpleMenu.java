package quiz.meal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

import quiz.meal.model.Item;

public class SimpleMenu implements Menu {
    private Map<String, Item> items;

    @SuppressWarnings("unchecked")
    public SimpleMenu() {
        items = Collections.unmodifiableMap((Map<String, Item>) Yaml.load(OrderMaker.class.getResourceAsStream("food.yml")));
    }
    
    /* (non-Javadoc)
     * @see quiz.meal.Menu#getAllItems()
     */
    public Map<String, Item> getAllItems() {
        return items;
    }
    
    /* (non-Javadoc)
     * @see quiz.meal.Menu#getItems(java.lang.String)
     */
    public List<Item> getItems(String ... names) {
        List<Item> itemList = new ArrayList<Item>();

        for(String name : names) {
            Item item = items.get(name);            
            if (item == null) {
                throw new IllegalArgumentException("Sorry no such item on menu, please try others!");
            }
   
            itemList.add(item);
        }        
        return itemList; 
    }
    
    /* (non-Javadoc)
     * @see quiz.meal.Menu#getOrderPrice(java.util.List)
     */
    public double getOrderPrice(List<Item> items) {
        double price = 0.0;
        for(Item i : items) {
            price += i.getPrice();
        }
        
        return price;
    }
}
