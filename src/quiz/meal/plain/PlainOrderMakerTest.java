package quiz.meal.plain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import quiz.meal.AbstractOrderMakerTest;
import quiz.meal.OrderMaker;
import quiz.meal.model.Item;
import quiz.meal.model.Meal;

/**
 * 
 * @author Jacky See
 * Test case for Plain Order Maker
 */
public class PlainOrderMakerTest extends AbstractOrderMakerTest {
	
	private PlainOrderMaker maker;
	
	@Before
	public void setup(){
		maker = new PlainOrderMaker();
	}
	
	
	@Test
	public void testMatchMeal() throws Exception {
		List<Item> foodItems = getSimpleMenu().getItems("雙層芝士孖堡","中薯條","中可樂");
		Meal meal = (Meal) getSimpleMenu().getItems("雙層芝士孖堡套餐").get(0);
		assertTrue(maker.matchMealItems(foodItems, meal));
		
		foodItems = getSimpleMenu().getItems("至尊漢堡","中薯條","中可樂");
		meal = (Meal) getSimpleMenu().getItems("雙層芝士孖堡套餐").get(0);
		assertFalse(maker.matchMealItems(foodItems, meal));
		
		foodItems = getSimpleMenu().getItems("雙層芝士孖堡","中薯條","中可樂","中可樂");
		meal = (Meal) getSimpleMenu().getItems("雙層芝士孖堡套餐").get(0);
		assertTrue(maker.matchMealItems(foodItems, meal));
	}
	
	@Test
	public void testWorthiestMeal() throws Exception {
		List<Item> foodItems = getSimpleMenu().getItems("雙層芝士孖堡","中薯條","中可樂","至尊漢堡");
		Meal worthiestMeal = new PlainOrderMaker().getWorthiestMeal(foodItems);
		assertEquals(worthiestMeal.getName(),"至尊漢堡套餐");
	}
	
	@Test
	public void testRemainingFood() throws Exception {
		List<Item> foodItems = getSimpleMenu().getItems("雙層芝士孖堡","中薯條","中可樂","中可樂","至尊漢堡");
		Meal meal = (Meal) getSimpleMenu().getItems("雙層芝士孖堡套餐").get(0);
		new PlainOrderMaker().subtractMealFoodFromItemList(meal, foodItems);
		assertEquals(2, foodItems.size());
	}

	@Override
	public OrderMaker getOrderMaker() {
		return new PlainOrderMaker();
	}

}
