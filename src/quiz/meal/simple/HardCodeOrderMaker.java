package quiz.meal.simple;

import java.util.Arrays;
import java.util.List;

import quiz.meal.Menu;
import quiz.meal.SimpleMenu;
import quiz.meal.OrderMaker;
import quiz.meal.model.Item;

public class HardCodeOrderMaker extends NaiveOrderMaker implements OrderMaker {

    @Override
    public List<Item> order(Item... orderItem) {
        Menu simpleMenu = new SimpleMenu();
        List<Item> orderItemList = Arrays.asList(orderItem);
        
        if (orderItemList.equals(simpleMenu.getItems("雙層芝士孖堡", "中薯條", "中可樂", "至尊漢堡", "中薯條", "中可樂"))) {
            return simpleMenu.getItems("雙層芝士孖堡套餐", "至尊漢堡套餐");
        }
        
        if (orderItemList.equals(simpleMenu.getItems("雙層芝士孖堡", "中薯條", "至尊漢堡", "中薯條", "中可樂"))) {
            return simpleMenu.getItems("至尊漢堡套餐", "中薯條", "雙層芝士孖堡");
        }
        
        if (orderItemList.equals(simpleMenu.getItems("雙層芝士孖堡", "至尊漢堡", "中薯條", "中可樂"))) {
            return simpleMenu.getItems("雙層芝士孖堡", "至尊漢堡套餐");
        }

        if (orderItemList.equals(simpleMenu.getItems("雙層芝士孖堡", "中薯條", "中可樂"))) {
            return simpleMenu.getItems("雙層芝士孖堡套餐");
        }
        
        return super.order(orderItem);
    }

}
