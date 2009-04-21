package quiz.meal.model;

import java.util.List;


public class Meal implements Item {
    private String name;
    private double price;
    private List<Food> food;
    
    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the price
     */
    @Override
    public double getPrice() {
        return price;
    }
    /**
     * @param price the price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }
    /**
     * @return the food
     */
    public List<Food> getFood() {
        return food;
    }
    /**
     * @param food the food to set
     */
    public void setFood(List<Food> food) {
        this.food = food;
    }
    
    public String toString() {
        return String.format("Meal(%s,%.2f)", name, price);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Meal other = (Meal) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
 
}
