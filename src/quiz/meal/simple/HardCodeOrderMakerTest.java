package quiz.meal.simple;


import quiz.meal.AbstractOrderMakerTest;
import quiz.meal.OrderMaker;

public class HardCodeOrderMakerTest extends AbstractOrderMakerTest { 
    
    public OrderMaker getOrderMaker() {
        return new HardCodeOrderMaker();
    }
}
