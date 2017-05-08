import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Holds the data from elements
 * <p>
 * Started on 13-4-2017<br>
 * Last changes made on 8-5-2017
 *
 * @author Thomas Holleman
 */
public class Element {
    private static int highestId = 0;
    
    private final int id;
    private String name;
    private ArrayList<String> recipes;
    private Category category;
    private boolean basic;
    
    public Category getCategory() {
        return category;
    }
    
    public Element(String name) {
        this(name, ++highestId, false);
    }
    
    public Element(String name, int id, boolean basic) {
        this.name = name;
        recipes = new ArrayList<>();
        this.id = id;
        this.basic = basic;
        category = null;
        
        if (id > highestId) {
            highestId = id;
        }
    }
    
    /**
     * Should only be used if there are no elements saved.
     */
    public static void resetCounter() {
        highestId = 0;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public boolean isBasic() {
        return basic;
    }
    
    /**
     * Adds a recipe for easy exporting later on
     *
     * @param toAdd a recipe following the format: [int id],[int id]
     */
    public void addRecipe(String toAdd) {
        recipes.add(toAdd);
    }
    
    @Override
    public String toString() {
        return "[" + id + ". " + name + "]";
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public ArrayList<String> getRecipes() {
        return recipes;
    }
    
    public boolean isKnown() {
        return isBasic() || category != null && category.getElementById(id, true) != null;
    }
    
    /**
     * Creates an string that can be used to export this element to a safe file
     *
     * @return a valid export string
     */
    public String exportLine() {
        String known = basic ? "b" : isKnown() ? "k" : "u";
        return known + ";" + id + ";" + name + ";" + getRecipesString();
    }
    
    /**
     * Creates an string that can be used to export the recipes to a safe file
     *
     * @return A valid export string
     */
    @NotNull
    public String getRecipesString() {
        StringBuilder output = new StringBuilder();
        // Appends the recipes together
        for (String recipe : recipes) {
            output.append(recipe).append(";");
        }
        return output.toString();
    }
    
    public void removeRecipe(String recipe) {
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).equals(recipe)) {
                recipes.remove(i);
            }
        }
    }
}
