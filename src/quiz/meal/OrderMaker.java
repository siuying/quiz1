package quiz.meal;

import java.util.List;

import quiz.meal.model.Item;

public interface OrderMaker {
    List<Item> order(Item ... wantedItems);

}
