package quiz.meal.csp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

public class CSOrderMaker implements OrderMaker {
    private Logger log = Logger.getLogger(CSOrderMaker.class.getName());    
    private Menu menu;
    private Item[] allItems;

    public CSOrderMaker() {
        menu = new SimpleMenu();
        allItems = menu.getAllItems().values().toArray(new Item[0]);
    }

    @Override
    public List<Item> order(Item... wantedItems) {
        CspSolver solver = CspSolver.createSolver();
        CspVariableFactory varFactory = solver.getVarFactory();
        solver.setAutoPropagate(true);

        List<Food> wantedFood = OrderHelper.getItemAsFoodList(Arrays.asList(wantedItems));
        Map<Item, Integer> foodCount = OrderHelper.getItemCount(wantedFood);
        Item[] items = getRelatedItems(wantedItems);
        
        List<CspIntVariable> orderVar = new ArrayList<CspIntVariable>();
        for (Item f : items) {
            CspIntVariable w = varFactory.intVar(f.getName(), 0, wantedFood.size());
            orderVar.add(w);
        }
        int maxPrice = ((int) maxPrice(wantedFood) * 10);
        CspIntVariable priceVar = varFactory.intVar("price", 0, maxPrice);
        CspIntExpr priceExpr = priceVar;

        try {
            for (int i = 0; i < items.length; i++) {
                Item item = items[i];
                CspIntVariable oX = orderVar.get(i);
                if (item instanceof Food) {
                    CspIntExpr sum = oX;
                    for (int j = 0; j < items.length; j++) {
                        if (items[j] instanceof Meal) {
                            Meal m = (Meal) items[j];
                            if (m.getFood().contains(item)) {
                                sum = sum.add(orderVar.get(j));
                            }
                        }
                    }
                    
                    // ordered_food(X) + SUM(ordered_meal_with_food(X)) >= wanted_food(x)
                    int itemCount = foodCount.containsKey(item) ? foodCount.get(item) : 0;
                    CspConstraint foodConstraint = sum.geq(itemCount);
                    solver.addConstraint(foodConstraint);
                }
            
                // total price = sum ordered_food(X) * price(X)
                priceExpr = priceExpr.add(oX.multiply((int)(item.getPrice()*10)));
            }
            log.debug("price = " + priceExpr.toString());
            
            solver.propagate();
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
    
    private double maxPrice(List<Food> wantedFood) {
        double price = 0.0;
        for(Food f : wantedFood) {
            price += f.getPrice();
        }
        return price;
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
    
    /**
     * Filter out items that are not related to the wanted food. "Related to" means the item is a wanted food, 
     * or a meal containing wanted food. 
     * 
     * @param allItems all items
     * @param wantedFood food wanted
     * @return array of items that are only related to the wanted order
     */
    private Item[] getRelatedItems(Item[] wantedFood) {
        ArrayList<Item> itemList = new ArrayList<Item>(Arrays.asList(allItems));
        Iterator<Item> iter = itemList.iterator();
        while(iter.hasNext()) {
            Item item = iter.next();
            boolean isWanted = false;
            for(Item wanted : wantedFood) {
                if (item.equals(wanted) || (item instanceof Meal && ((Meal)item).getFood().contains(wanted))) {
                    // retain
                    isWanted = true;
                    break;
                }
            }
            if (!isWanted) {
                iter.remove();
            }
        }
        
        return itemList.toArray(new Item[itemList.size()]);
    }
}
