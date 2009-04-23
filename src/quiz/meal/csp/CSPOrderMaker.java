package quiz.meal.csp;

import static choco.Choco.eq;
import static choco.Choco.makeIntVar;
import static choco.Choco.scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import quiz.meal.Menu;
import quiz.meal.OrderHelper;
import quiz.meal.OrderMaker;
import quiz.meal.model.Food;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;
import quiz.meal.simple.NaiveOrderMaker;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

/**
 * Create a CSP/Integer Linear Programming based order maker.
 * 
 * @author siuying
 * @see http://www.reality.hk/articles/2009/04/22/960/
 *
 */
public class CSPOrderMaker extends NaiveOrderMaker implements OrderMaker {
    private Logger log = Logger.getLogger(CSPOrderMaker.class.getName());
    private Item[] items;
    private Menu menu;
    
    // maximum price per order item, in dollars
    private static int MAX_ORDER_ITEM_PRICE = 50;
    private static int TIME_LIMIT = 60000;
    
    public CSPOrderMaker() {
    }
    
    public CSPOrderMaker(Menu menu) {
        this.menu = menu;
    }
    
    /**
     * Find best value order, using CSP
     */
    @Override
    public List<Item> order(Item... wantedItems) {        
        Model model = new CPModel();
        int maxOrder = wantedItems.length;
        List<Food> wantedLists = OrderHelper.getItemAsFoodList(Arrays.asList(wantedItems));

        // optimization: remove any unrelated item from our "all item list"
        items = getRelatedItems(this.menu.getAllItems(), wantedItems);
        
        // create variables for items to be ordered
        List<IntegerVariable> orderItemCountVar = new ArrayList<IntegerVariable>();
        for(int i=0; i<items.length; i++) {
            IntegerVariable order = makeIntVar(items[i].getName(), 0, maxOrder);
            orderItemCountVar.add(order);
            model.addVariable(order);
        }

        // Constraints: number of returned food = orderItemCount[food] + orderItemCount[meal] which meal contains food
        addFoodConstraint(model, wantedLists, orderItemCountVar);
        
        // Constraint: total price = sum of price * orderItemCount
        IntegerVariable totalPrice = makeIntVar("totalPrice", 0, maxOrder * MAX_ORDER_ITEM_PRICE * 10);
        model.addConstraint(eq(scalar(getPriceList(), orderItemCountVar.toArray(new IntegerVariable[0])), 
                totalPrice));

        // Build a solver and solve the problem, based on minimized total price
        Solver s = buildSolverByMinimizeTotalPrice(model, totalPrice);
        if (s.solve()) {
            return getOrderBySolution(s, orderItemCountVar);
        } else {
            log.warning("no solution or cannot attained the solution with timeout limit");
            return super.order(wantedItems);
        }
    }
    
    /**
     * Build a CSP solver
     * @param model
     * @param totalPrice
     * @return
     */
    private Solver buildSolverByMinimizeTotalPrice(Model model, IntegerVariable totalPrice) {
        Solver s = new CPSolver();
        s.read(model);
        s.setValIntIterator(new IncreasingDomain());
        s.minimize(s.getVar(totalPrice), false);
        s.setTimeLimit(TIME_LIMIT);
        return s;
    }
    
    /**
     * Based on CSP solution, create list of order item
     * 
     * @param solver CSP solver
     * @param orderItemCount variables for item
     * @return list of order item
     */
    private List<Item> getOrderBySolution(Solver solver, List<IntegerVariable> orderItemCount) {
        List<Item> orderItems = new ArrayList<Item>();
        for(int i=0; i<items.length; i++) {
            // number of order item i
            int val = solver.getVar(orderItemCount.get(i)).getVal();
            
            // add order
            for(int j=0; j<val; j++) {
                orderItems.add(menu.getItems(solver.getVar(orderItemCount.get(i)).getName()).get(0));
            }
        }        
        return orderItems;
    }
    
    /**
     * Array of price of order item. Price is double, but as the solver do not
     * support floating point number , we will convert the price to integer
     * value (per 10 cents)
     * 
     * @return price of order items
     */
    private int[] getPriceList() {
        int[] priceList = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            priceList[i] = (int) (items[i].getPrice() * 10);
        }
        return priceList;
    }
    
    /**
     * Add Food Number Constraint. Number of ordered food must be equal to or greater than that of wanted food
     * 
     * @param model the CSP Model
     * @param wantedLists list of wanted food    
     * @param orderItemCount Model variables representing the resulting item order
     */
    private void addFoodConstraint(Model model, List<Food> wantedLists, List<IntegerVariable> orderItemCount) {
        int maxOrder = wantedLists.size();
        Map<Item, Integer> itemCount = OrderHelper.getItemCount(wantedLists);
        List<IntegerVariable> wantedItemCountVar = new ArrayList<IntegerVariable>();
        for(int i=0; i<items.length; i++) {            
            Item item = items[i];            
            if (itemCount.containsKey(item)) {
                wantedItemCountVar.add(makeIntVar(item.getName(), itemCount.get(item), maxOrder));
            } else {
                wantedItemCountVar.add(makeIntVar(item.getName(), 0, maxOrder));
            }
        }
        
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
        
        IntegerVariable[] orderItemCountArr = orderItemCount.toArray(new IntegerVariable[0]);
        for(int i=0; i<items.length; i++) {
            model.addConstraint(Choco.geq(scalar(foodMealMatrix[i], orderItemCountArr), 
                    wantedItemCountVar.get(i)));
        }
    }
    
    /**
     * Filter out items that are not related to the wanted food. "Related to" means the item is a wanted food, 
     * or a meal containing wanted food. 
     * 
     * @param allItems all items
     * @param wantedFood food wanted
     * @return array of items that are only related to the wanted order
     */
    private Item[] getRelatedItems(Map<String, Item> allItems, Item[] wantedFood) {
        ArrayList<Item> itemList = new ArrayList<Item>(allItems.values());        
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

    /**
     * @param menu the menu to set
     */
    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
