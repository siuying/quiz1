package quiz.meal.search;

import quiz.meal.AbstractOrderMakerTest;
import quiz.meal.OrderMaker;
import quiz.meal.SimpleMenu;

public class TreeSearchOrderMakerTest extends AbstractOrderMakerTest {
    public OrderMaker getOrderMaker() {
        return new TreeSearchOrderMaker(new SimpleMenu());
    }
}
