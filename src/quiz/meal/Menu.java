package quiz.meal;

import java.util.List;
import java.util.Map;

import quiz.meal.model.Item;

public interface Menu {

    Map<String, Item> getAllItems();

    List<Item> getItems(String... names);

    double getOrderPrice(List<Item> items);

}