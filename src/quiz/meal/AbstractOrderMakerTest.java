
package quiz.meal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ho.yaml.Yaml;
import org.junit.Before;
import org.junit.Test;

import quiz.meal.model.Food;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;
import quiz.meal.simple.NaiveOrderMakerTest;

public abstract class AbstractOrderMakerTest {
    private Menu simpleMenu;
    private OrderMaker maker;    
    private Logger log = Logger.getLogger(NaiveOrderMakerTest.class.getName());
    
    @Before
    public void setUp() throws Exception {
        simpleMenu = new SimpleMenu();
        maker = getOrderMaker();
    }

    @Test
    public void testSimpleOrder() {
        List<Item> wantedItems = getSimpleMenu().getItems("至尊漢堡");        
        List<Item> expectedItems = getSimpleMenu().getItems("至尊漢堡");
        baseOrderTest(wantedItems, expectedItems);
    }
    
    @Test
    public void testOrderWithMeal() {
        List<Item> wantedItems = getSimpleMenu().getItems("雙層芝士孖堡", "中薯條", "中可樂");
        List<Item> expectedItems = getSimpleMenu().getItems("雙層芝士孖堡套餐");
        baseOrderTest(wantedItems, expectedItems);
    }
    
    @Test
    public void testChooseCheapMeal() {
        List<Item> wantedItems = getSimpleMenu().getItems("雙層芝士孖堡", "至尊漢堡", "中薯條", "中可樂");
        List<Item> expectedItems = getSimpleMenu().getItems("雙層芝士孖堡", "至尊漢堡套餐");
        baseOrderTest(wantedItems, expectedItems);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testOrders() {
        List<List<List<String>>> list = (List<List<List<String>>>) Yaml.load(OrderMaker.class.getResourceAsStream("testcase.yml"));
        for(List<List<String>> testLists : list) {
            List<Item> wantedItems = getSimpleMenu().getItems(testLists.get(0).toArray(new String[0]));
            List<Item> expectedItems = getSimpleMenu().getItems(testLists.get(1).toArray(new String[0]));
            baseOrderTest(wantedItems, expectedItems);
        }        
    }
    
    private void baseOrderTest(List<Item> wantedItems, List<Item> expectedItems) {
        log.info("testing " + wantedItems +", expects " + expectedItems);
        List<Item> orderItems = maker.order(wantedItems.toArray(new Item[0]));
        
        assertTrue("missing wanted items in " + wantedItems, validateOrder(expectedItems, orderItems));
        assertEquals("not minimum price for " + wantedItems, getSimpleMenu().getOrderPrice(expectedItems), getSimpleMenu().getOrderPrice(orderItems), 0.01);
    }
    
    private boolean validateOrder(List<Item> wantedItems, List<Item> orderItems) {
        wantedItems = getItemAsFoodList(wantedItems);
        orderItems = getItemAsFoodList(orderItems);
        
        Map<Item, Integer> wantedItemsCount = getItemCount(wantedItems);
        Map<Item, Integer> orderItemsCount = getItemCount(orderItems);
        
        for(Item key : wantedItemsCount.keySet()) {
            if (orderItemsCount.get(key) == null ||
                    orderItemsCount.get(key) < wantedItemsCount.get(key)) {

                log.warning("missing " + key.getName() + " frolm order");
                return false;                
            }
        }
        
        return true;
    }

    private List<Item> getItemAsFoodList(List<? extends Item> items) {
        List<Item> itemList = new ArrayList<Item>();
        for(Item i : items) {
            if (i instanceof Food) {
                itemList.add(i);
            } else if (i instanceof Meal) {
                List<Food> mealFood = ((Meal) i).getFood();
                itemList.addAll(getItemAsFoodList(mealFood));
            } else {
                throw new AssertionError("Unexpected type");
            }
        }
        return itemList;
    }
    
    private Map<Item, Integer> getItemCount(List<Item> items) {
        Map<Item, Integer> itemCount = new HashMap<Item, Integer>();
        for(Item i : items) {
            if (!itemCount.containsKey(i)) {
                itemCount.put(i, 1);
            } else {
                itemCount.put(i, itemCount.get(i) + 1);
            }
        }
        return itemCount;
    }
    
    /**
     * return your implementation of OrderMaker here
     * @return
     */
    public abstract OrderMaker getOrderMaker();


	public Menu getSimpleMenu() {
		return simpleMenu;
	}

}
