package quiz.meal.plain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import quiz.meal.OrderMaker;
import quiz.meal.SimpleMenu;
import quiz.meal.model.Food;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;

/**
 * @author Jacky See
 * Making orders arranged by worthies meal
 */
public class PlainOrderMaker implements OrderMaker {
	
	private static SimpleMenu menu = new SimpleMenu();
	private List<Meal> meals;
	
	public PlainOrderMaker() {
		meals = getMenusSortedByMoneySaved();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Item> order(Item... wantedItems) {
		
		List<Item> resultItems = new ArrayList<Item>();
		List<Item> remainingFoods = (List<Item>) new ArrayList<Item>(Arrays.asList(wantedItems)).clone();
		
		Meal meal = getWorthiestMeal(remainingFoods);
		while(meal != null){
			resultItems.add(meal);
			subtractMealFoodFromItemList(meal, remainingFoods);
			meal = getWorthiestMeal(remainingFoods);
		}
		
		resultItems.addAll(remainingFoods);
		return resultItems;
	}
	
	/**
	 * Get worthiest meal by comparing individual total - meal price
	 * @param wantedItems
	 * @return
	 */
	public Meal getWorthiestMeal(List<Item> wantedItems){
		for(Meal meal: meals){
			if(matchMealItems(wantedItems, meal)){
				return meal;
			}
		}
		return null;
	}
	
	/**
	 * Remove meal items from the input list
	 * @param meal
	 * @param items
	 */
	public void subtractMealFoodFromItemList(Meal meal, List<Item> items){
		for (Item item : meal.getFood()) {
			items.remove(item);
		}
	}
	
	/**
	 * Get money saved by a meal
	 * @param meal
	 * @return
	 */
	public double getMoneySaved(Meal meal){
		if(meal == null) 
			return 0;
		
		double individualTotal = 0;
		for (Item food : meal.getFood()) {
			individualTotal += food.getPrice();
		}
		return individualTotal - meal.getPrice();
	}
	
	
	/**
	 * See if the items can form the input meal
	 * @param wantedItems
	 * @param meal
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean matchMealItems(List<Item> wantedItems, Meal meal){
		List<Item> itemList = (List<Item>) new ArrayList<Item>(wantedItems).clone();
    	boolean match = true;
    	for (Food f : meal.getFood()) {
			if(!itemList.contains(f)){
				return false;
			}
			else{
				itemList.remove(f);
			}
		}
    	return match;
	}
	
	
	/**
	 * Get Meals sorted by money saved
	 * @return
	 */
	public List<Meal> getMenusSortedByMoneySaved(){
		List<Meal> meals = new ArrayList<Meal>();
		for(Item item : menu.getAllItems().values()){
			if(item instanceof Meal){
				meals.add((Meal)item);
			}
		}
		Collections.sort(meals, new Comparator<Meal>(){
			@Override
			public int compare(Meal m1, Meal m2) {
				double result = getMoneySaved(m2) - getMoneySaved(m1);
				return (result > 0)? 1: (result == 0)? 0: -1;
			}
		});
		return meals;
	}
	
}
