package quiz.meal.simple;

import java.util.Arrays;
import java.util.List;

import quiz.meal.OrderMaker;
import quiz.meal.model.Item;

public class NaiveOrderMaker implements OrderMaker {

    @Override
    public List<Item> order(Item ... orderItem) {
        return Arrays.asList(orderItem);
    }

}
