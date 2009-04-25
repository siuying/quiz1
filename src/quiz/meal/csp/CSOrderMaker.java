package quiz.meal.csp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jopt.csp.CspSolver;
import jopt.csp.search.SearchAction;
import jopt.csp.search.SearchActions;
import jopt.csp.search.SearchGoal;
import jopt.csp.search.SearchGoals;
import jopt.csp.search.SearchTechniques;
import jopt.csp.variable.CspConstraint;
import jopt.csp.variable.CspIntExpr;
import jopt.csp.variable.CspIntVariable;
import jopt.csp.variable.CspVariableFactory;
import jopt.csp.variable.PropagationFailureException;

import org.apache.log4j.Logger;

import quiz.meal.Menu;
import quiz.meal.OrderHelper;
import quiz.meal.OrderMaker;
import quiz.meal.SimpleMenu;
import quiz.meal.model.Food;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;

/**
 * Use Integer Linear Programming method to find cheapest orders for specified order items.
 * 
 * @author siuying
 * @see http://www.reality.hk/articles/2009/04/22/960/
 */
public class CSOrderMaker implements OrderMaker {
    private Logger log = Logger.getLogger(CSOrderMaker.class.getName());    
    private Menu menu;
    private Item[] items;

    public CSOrderMaker() {
        menu = new SimpleMenu();
        items = menu.getAllItems().values().toArray(new Item[0]);
    }

    @Override
    public List<Item> order(Item... wantedItems) {
        CspSolver solver = CspSolver.createSolver();
        CspVariableFactory varFactory = solver.getVarFactory();
        solver.setAutoPropagate(true);
        
        List<Food> wantedFood = OrderHelper.getItemAsFoodList(Arrays.asList(wantedItems));
        Map<Item, Integer> foodCount = OrderHelper.getItemCount(wantedFood);
        
        // create variable for order items
        List<CspIntVariable> orderVar = new ArrayList<CspIntVariable>();
        for (Item f : items) {
            CspIntVariable w = varFactory.intVar(f.getName(), 0, wantedFood.size());
            orderVar.add(w);
        }
        
        // add constraints
        CspIntExpr priceExpr = null;
        try {
            // ordered_food(X) + SUM(ordered_meal_with_food(X)) = wanted_food(x)
            for (int i = 0; i < items.length; i++) {
                Item item = items[i];
                if (item instanceof Food) {
                    createFoodConstraint(solver, foodCount, orderVar, orderVar.get(i), (Food) item);
                }                
            }
            
            // total price = sum ordered_food(X) * price(X)
            // use integer because jOpt cannot work with float!
            for (int i = 0; i < items.length; i++) {
                Item item = items[i];
                CspIntVariable oX = orderVar.get(i);

                int itemPrice = (int) item.getPrice() * 10;
                if (priceExpr == null) {
                    priceExpr = oX.multiply(itemPrice);
                } else {
                    priceExpr = priceExpr.add(oX.multiply(itemPrice));
                }
            }

        } catch (PropagationFailureException e) {
            log.error("error propagate constraint", e);

        }
        
        SearchTechniques tech = solver.getSearchTechniques();
        SearchGoals goals = solver.getSearchGoals();
        SearchGoal minimizePriceGoal = goals.minimize(priceExpr);

        SearchActions actions = solver.getSearchActions();
        SearchAction action = actions.generate(orderVar.toArray(new CspIntVariable[0]));            
        solver.solve(action, minimizePriceGoal, tech.dfs());
        log.info(" result order = " + orderVar);

        return getOrderBySolution(items, orderVar);
    }
    
    private void createFoodConstraint(CspSolver solver, Map<Item, Integer> foodCount, 
            List<CspIntVariable> orderVar, CspIntVariable oX, Food food) throws PropagationFailureException {
        CspIntExpr sum = oX;
        for (int j = 0; j < items.length; j++) {
            if (items[j] instanceof Meal) {
                Meal meal = (Meal) items[j];
                if (meal.getFood().contains(food)) {
                    sum = sum.add(orderVar.get(j));
                }
            }
        }
        
        int itemCount = foodCount.containsKey(food) ? foodCount.get(food) : 0;
        CspConstraint foodConstraint = sum.eq(itemCount);
        solver.addConstraint(foodConstraint);
        
        if (log.isDebugEnabled()) {
            log.debug(String.format("food(%s): %s", food.getName(), foodConstraint));
        }
    }
    
    private List<Item> getOrderBySolution(Item[] items, List<CspIntVariable> orderItemCount) {
        List<String> orderItemNames = new ArrayList<String>();
        for(int i=0; i<items.length; i++) {
            // number of order item i
            int val = orderItemCount.get(i).getMin();            
            // add order
            for(int j=0; j<val; j++) {
                orderItemNames.add(orderItemCount.get(i).getName());
            }
        }
        return menu.getItems(orderItemNames.toArray(new String[orderItemNames.size()]));
    }
}
