package quiz.meal.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import quiz.meal.Menu;
import quiz.meal.OrderMaker;
import quiz.meal.model.Food;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;

/**
 * Create a search based order maker.
 * 
 * @author Jesse Mok (Hin Ba)
 * @see http://mysinablog.com/index.php?op=ViewArticle&articleId=1688386
 *
 */

public class TreeSearchOrderMaker implements OrderMaker {
	private Menu menu;
	private Meal[] meals;
	private double maxMoneySaved;
	private Stack<Meal> tracker = new Stack<Meal>();
	private ArrayList<Meal> mealToBuy = new ArrayList<Meal>();
	private ArrayList<Food> foodToBuy = new ArrayList<Food>();
	
    public TreeSearchOrderMaker(Menu menu) {
        this.menu = menu;

        ArrayList<Meal> mealList = new ArrayList<Meal>();
        Map<String, Item> map = menu.getAllItems();
        Set<String> names = map.keySet();
        for (String name : names) {
        	Item item = map.get(name);
        	if (item instanceof Meal) {
        		mealList.add((Meal)item);
        	}
        }
        meals = mealList.toArray(new Meal[mealList.size()]);
    }

    @Override
	public List<Item> order(Item... wantedItems) {
    	maxMoneySaved = 0;
    	foodToBuy.clear();
    	mealToBuy.clear();
    	double moneySaved = 0d;
    	for (Item item : wantedItems) {
    		foodToBuy.add((Food)item);
    	}
    	List<Food> foodRemain = new ArrayList<Food>(foodToBuy);
		tryAllMeal(0, foodRemain, moneySaved);
		
		List<Item> mealAndFood = new ArrayList<Item>();
		mealAndFood.addAll(mealToBuy);
		mealAndFood.addAll(foodToBuy);
		
		return (List<Item>)mealAndFood;
	}
    
    /*
     *   Try to combine 1 meal by the remain food
     */
    public boolean tryAllMeal(int mealIndex, List<Food> foodRemain, double moneySaved) {
    	boolean makeNewCombo = false;
		for (int i = mealIndex; i < meals.length; i++) {
			makeNewCombo |= tryCombine(i, foodRemain, moneySaved);
		}
		return makeNewCombo;
    }
    
    /*
     *   Find a meal from the remain food.
     *   If found
     *     try to combine more by tryAllMeal().
     *     If can't combine more
     *     	 this is one possible case
     *   
     */
    public boolean tryCombine(int mealIndex, List<Food> foodRemain, double moneySaved) {
		Meal targetMeal = meals[mealIndex];
    	List<Food> newFoodRemain = new ArrayList<Food>(foodRemain);
    	boolean canCombine = true;

    	for (Food food: targetMeal.getFood()) {
    		if (!newFoodRemain.remove(food)) {
    			canCombine = false;
    			break;
    		}
    	}

    	if (canCombine) {
    		moneySaved = moneySaved + menu.getOrderPrice(new ArrayList<Item>(targetMeal.getFood())) - targetMeal.getPrice();
    		tracker.push(meals[mealIndex]);
			
    		if (!tryAllMeal(mealIndex, newFoodRemain, moneySaved)) {
        		if (moneySaved > maxMoneySaved) {
        			maxMoneySaved = moneySaved;
        			mealToBuy = new ArrayList<Meal>(tracker);
        			foodToBuy = new ArrayList<Food>(newFoodRemain);
        		}
    		}
    		tracker.pop();
    		return true;
    	} else {
    		return false;
    	}
    }
}
