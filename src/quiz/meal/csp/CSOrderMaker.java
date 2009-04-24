package quiz.meal.csp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jopt.csp.CspSolver;
import jopt.csp.variable.CspConstraint;
import jopt.csp.variable.CspIntExpr;
import jopt.csp.variable.CspIntVariable;
import jopt.csp.variable.CspMath;
import jopt.csp.variable.CspVariableFactory;
import quiz.meal.Menu;
import quiz.meal.OrderHelper;
import quiz.meal.OrderMaker;
import quiz.meal.SimpleMenu;
import quiz.meal.model.Food;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;

public class CSOrderMaker implements OrderMaker {
	private Menu menu;
	
	public CSOrderMaker() {
		menu = new SimpleMenu();
	}
	
	@Override
	public List<Item> order(Item... wantedItems) {
        CspSolver solver = CspSolver.createSolver();
        CspVariableFactory varFactory  = solver.getVarFactory();
        CspMath varMath = varFactory.getMath();
        
        List<Food> wantedFood = OrderHelper.getItemAsFoodList(Arrays.asList(wantedItems));
        Map<Item, Integer> foodCount = OrderHelper.getItemCount(wantedFood);
        Item[] items = menu.getAllItems().values().toArray(new Item[0]);
        
        List<CspIntVariable> orderVar = new ArrayList<CspIntVariable>();
        for(Item f : items) {
        	CspIntVariable w = varFactory.intVar(f.getName(), 
        			foodCount.get(f) == null ? 0 : foodCount.get(f), 
					wantedFood.size());
        	orderVar.add(w);
        }
        
        // wantedX <= foodX + mean coontains foodX
        int[][] foodMealMatrix = new int[items.length][items.length];
        for(int i=0; i<items.length; i++) {
            Item item = items[i];
            if (item instanceof Food) {
                foodMealMatrix[i][i] = 1;

            } else if (item instanceof Meal) {
                Meal meal = (Meal) item;
                for(int j=0; j<items.length; j++) {
                    List<Food> mealFood = meal.getFood();
                    for(int k=0; k<mealFood.size(); k++) {
                        if ( mealFood.get(k).equals((items[j])) ) {
                            foodMealMatrix[j][i]++;
                        }
                    }

                }
            }
        }
        
        for(int i=0; i<items.length; i++) {
        	Item item = items[i];
        	CspIntVariable oX = orderVar.get(i);
        	CspIntExpr sum = oX;
        	
        	for(int j=0; j<items.length; j++) {
        		if (items[j] instanceof Meal && ((Meal)items[j]).getFood().contains(items[i])) {
        			sum = sum.add(orderVar.get(i));
        		}
        	}
        	
        	CspConstraint constraint = sum.geq(foodCount.get(item));
        }
        
//        for(int i=0; i<items.length; i++) {
//            model.addConstraint(Choco.geq(scalar(foodMealMatrix[i], orderItemCountArr), 
//                    wantedItemCountVar.get(i)));
//        }

        
        return null;
	}

}
