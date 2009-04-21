package quiz.meal.csp;

import quiz.meal.AbstractOrderMakerTest;
import quiz.meal.OrderMaker;
import quiz.meal.SimpleMenu;

public class CSPOrderMakerTest extends AbstractOrderMakerTest {
    public OrderMaker getOrderMaker() {
        return new CSPOrderMaker(new SimpleMenu());
    }
}
