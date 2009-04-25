package quiz.meal.csp;

import quiz.meal.AbstractOrderMakerTest;
import quiz.meal.OrderMaker;

public class CSOrderMakerTest extends AbstractOrderMakerTest {
    public OrderMaker getOrderMaker() {
        return new CSOrderMaker();
    }
}
