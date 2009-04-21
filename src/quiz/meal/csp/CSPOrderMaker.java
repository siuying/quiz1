package quiz.meal.csp;

import static choco.Choco.eq;
import static choco.Choco.makeIntVar;
import static choco.Choco.scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import quiz.meal.Menu;
import quiz.meal.OrderHelper;
import quiz.meal.OrderMaker;
import quiz.meal.model.Food;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

public class CSPOrderMaker implements OrderMaker {
    private Logger log = Logger.getLogger(CSPOrderMaker.class.getName());
    private Item[] items;
    private Menu menu;
    
    // maximum price per order item, in dollars
    private static int MAX_ORDER_ITEM_PRICE = 50;
    private static int TIME_LIMIT = 60000;
    
    public CSPOrderMaker(Menu menu) {
        this.menu = menu;
    }
    
    /**
     * Find best value order, using CSP
     */
    @Override
    public List<Item> order(Item... wantedItems) {
        int maxOrder = wantedItems.length;
        Model model = new CPModel();
        
        // optimization: remove any unrelated item from our "all item list"
        items = getRelatedItems(this.menu.getAllItems().values(), wantedItems);
        
        List<Food> wantedLists = OrderHelper.getItemAsFoodList(Arrays.asList(wantedItems));
        Map<Item, Integer> itemCount = OrderHelper.getItemCount(wantedLists);
        
        // create variables for items to be ordered and items wanted
        List<IntegerVariable> wantedItemCountVar = new ArrayList<IntegerVariable>();
        List<IntegerVariable> orderItemCountVar = new ArrayList<IntegerVariable>();
        for(int i=0; i<items.length; i++) {
            Item item = items[i];
            IntegerVariable order = makeIntVar(item.getName(), 0, maxOrder);
            orderItemCountVar.add(order);
            model.addVariable(order);
            
            int wantedItemCount = (itemCount.containsKey(item)) ? itemCount.get(item) : 0; 
            wantedItemCountVar.add(makeIntVar(item.getName(), wantedItemCount, maxOrder));
        }
        
        // Constraints: number of returned food = orderItemCount[food] + orderItemCount[meal] which meal contains food
        addFoodConstraint(model, orderItemCountVar, wantedItemCountVar);
        
        // Constraint: total price = sum of price * orderItemCount
        IntegerVariable totalPrice = makeIntVar("totalPrice", 0, maxOrder * MAX_ORDER_ITEM_PRICE * 10);
        model.addConstraint(eq(scalar(getPriceList(), orderItemCountVar.toArray(new IntegerVariable[0])), 
                totalPrice));

        Solver s = new CPSolver();
        s.read(model);
        
        s.setVarIntSelector(new StaticVarOrder(s.getVar(orderItemCountVar.toArray(new IntegerVariable[0]))));
        s.setValIntIterator(new IncreasingDomain());
        s.minimize(s.getVar(totalPrice), false);
        s.setTimeLimit(TIME_LIMIT);
        s.solve();
        s.printRuntimeSatistics();

        return getOrderBySolution(s, orderItemCountVar);
    }
    
    /**
     * Based on CSP solution, create order
     * 
     * @param solver
     * @param orderItemCount
     * @return
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
     * return price of items on menu
     * 
     * @return
     */
    private int[] getPriceList() {
        int[] priceList = new int[items.length];
        for(int i=0; i<items.length; i++) {
            priceList[i] = (int) (items[i].getPrice() * 10);
        }
        return priceList;
    }
    
    /**
     * Add Food Number Constraint. Ordered food must be equal to or greater than wanted food.
     * @param model
     * @param orderItemCount
     * @param wantedItemCount
     */
    private void addFoodConstraint(Model model, List<IntegerVariable> orderItemCount, List<IntegerVariable> wantedItemCount) {
        int[][] foodMealMatrix = new int[items.length][items.length];
        for(int i=0; i<items.length; i++) {
            Item item = items[i];
            if (item instanceof Food) {
                foodMealMatrix[i][i] = 1;
            } else if (item instanceof Meal) {
                Meal meal = (Meal) item;
                for(int j=0; j<items.length; j++) {
                    if (meal.getFood().contains(items[j])) {
                        foodMealMatrix[i][j]++;
                    }
                }
            }
        }
        
        for(int i=0; i<items.length; i++) {
            int[] itemThatProduceFood = new int[items.length];
            for(int j=0; j<items.length; j++) {
                itemThatProduceFood[j] = foodMealMatrix[j][i];
            }
            
            if (log.isLoggable(Level.FINER)) {
                log.fine("order " + items[i].getName() + " = " + Arrays.toString(itemThatProduceFood));
            }
            model.addConstraint(Choco.geq(scalar(itemThatProduceFood, orderItemCount.toArray(new IntegerVariable[0])), 
                    wantedItemCount.get(i)));
        }
    }
    

    @SuppressWarnings("unchecked")
    private Item[] getRelatedItems(Collection<Item> allItems, Item[] wantedItems) {
        ArrayList<Item> tmpItemList = new ArrayList<Item>(this.menu.getAllItems().values());
        ArrayList<Item> itemList = (ArrayList<Item>) tmpItemList.clone();
        for(Item i : tmpItemList) {
            boolean isWanted = false;
            for(Item wanted : wantedItems) {
                if (i.equals(wanted) || (i instanceof Meal && ((Meal)i).getFood().contains(wanted))) {
                    // retain
                    isWanted = true;
                    break;
                }
            }
            if (!isWanted) {
                itemList.remove(i);
            }
        }
        return itemList.toArray(new Item[itemList.size()]);
    }
}
