
package quiz.meal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ho.yaml.Yaml;
import org.junit.Before;
import org.junit.Test;

import quiz.meal.model.Food;
import quiz.meal.model.Item;
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
        List<Item> wantedItems = simpleMenu.getItems("至尊漢堡");        
        List<Item> expectedItems = simpleMenu.getItems("至尊漢堡");
        baseOrderTest(wantedItems, expectedItems);
    }
    
    @Test
    public void testOrderWithMeal() {
        List<Item> wantedItems = simpleMenu.getItems("雙層芝士孖堡", "中薯條", "中可樂");
        List<Item> expectedItems = simpleMenu.getItems("雙層芝士孖堡套餐");
        baseOrderTest(wantedItems, expectedItems);
    }
    
    @Test
    public void testChooseCheapMeal() {
        List<Item> wantedItems = simpleMenu.getItems("雙層芝士孖堡", "至尊漢堡", "中薯條", "中可樂");
        List<Item> expectedItems = simpleMenu.getItems("雙層芝士孖堡", "至尊漢堡套餐");
        baseOrderTest(wantedItems, expectedItems);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testOrders() {
        List<List<List<String>>> list = (List<List<List<String>>>) Yaml.load(OrderMaker.class.getResourceAsStream("testcase.yml"));
        for(List<List<String>> testLists : list) {
            List<Item> wantedItems = simpleMenu.getItems(testLists.get(0).toArray(new String[0]));
            List<Item> expectedItems = simpleMenu.getItems(testLists.get(1).toArray(new String[0]));
            baseOrderTest(wantedItems, expectedItems);
        }        
    }
    
    private void baseOrderTest(List<Item> wantedItems, List<Item> expectedItems) {
        log.info("testing " + wantedItems);
        List<Item> orderItems = maker.order(wantedItems.toArray(new Item[0]));
        log.info("   expects: " + expectedItems);
        log.info("   result:  " + orderItems);
        
        assertTrue("missing wanted items in " + wantedItems, validateOrder(expectedItems, orderItems));
        assertEquals("not minimum price for " + wantedItems, simpleMenu.getOrderPrice(expectedItems), simpleMenu.getOrderPrice(orderItems), 0.01);
    }
    
    private boolean validateOrder(List<Item> wantedItems, List<Item> orderItems) {
        List<Food> wantedItemsAsFood = OrderHelper.getItemAsFoodList(wantedItems);
        List<Food> orderItemsAsFood = OrderHelper.getItemAsFoodList(wantedItems);
        
        Map<Item, Integer> wantedItemsCount = OrderHelper.getItemCount(wantedItemsAsFood);
        Map<Item, Integer> orderItemsCount = OrderHelper.getItemCount(orderItemsAsFood);
        
        for(Item key : wantedItemsCount.keySet()) {
            if (orderItemsCount.get(key) == null ||
                    orderItemsCount.get(key) < wantedItemsCount.get(key)) {

                log.warning("missing " + key.getName() + " from order");
                return false;                
            }
        }
        
        return true;
    }
    
    /**
     * return your implementation of OrderMaker here
     * @return
     */
    public abstract OrderMaker getOrderMaker();

}
