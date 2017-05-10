import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Holds the data from elements
 * <p>
 * Started on 13-4-2017<br>
 * Last changes made on 10-5-2017
 *
 * @author Thomas Holleman
 */
public class Element
{
    private static int highestId = 0;
    
    private final int id;
    private String name;
    private ArrayList<String> recipes;
    private Category category;
    private boolean basic;
    
    /**
     * Simple constructor the class
     *
     * @param name The name of this element
     */
    public Element(String name)
    {
        this(name, ++highestId, false);
    }
    
    /**
     * Complete constructor for this class.
     * When used make sure the id is unique to avoid complications during run time
     *
     * @param name  The name of the element
     * @param id    The id of the element
     * @param basic True if the element is a basic element
     */
    public Element(String name, int id, boolean basic)
    {
        this.name = name;
        recipes = new ArrayList<>();
        this.id = id;
        this.basic = basic;
        category = null;
        
        // If the id is higher then the highest id: update the highest id
        if (id > highestId)
        {
            highestId = id;
        }
    }
    
    /**
     * Should only be used if all elements are deleted.
     */
    public static void resetCounter()
    {
        highestId = 0;
    }
    
    /**
     * Simple getter for the category of this element
     *
     * @return The category of this element or null
     */
    @Nullable
    public Category getCategory()
    {
        return category;
    }
    
    /**
     * Simple setter for the category of this element
     *
     * @param category The element that this should be set to
     */
    public void setCategory(Category category)
    {
        this.category = category;
    }
    
    /**
     * Adds a recipe for easy exporting later on
     *
     * @param toAdd a recipe following the format: [int id],[int id]
     */
    public void addRecipe(String toAdd)
    {
        recipes.add(toAdd);
    }
    
    /**
     * toString of this class with some of the information of this class to identify it
     *
     * @return An String to identify instances of this class
     */
    @Override
    public String toString()
    {
        return "[" + id + ". " + name + "]";
    }
    
    /**
     * Simple getter for the id
     *
     * @return The id of the instance
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Simple getter for the name
     *
     * @return The name of the instance
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Simple setter of the name
     *
     * @param name The new name of this instance
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Simple getter for the recipes
     *
     * @return The recipes of this instance
     */
    public ArrayList<String> getRecipes()
    {
        return recipes;
    }
    
    /**
     * Creates an string that can be used to export this element to a safe file
     *
     * @return a valid export string
     */
    public String exportLine()
    {
        String known = basic ? "b" : isKnown() ? "k" : "u";
        return known + ";" + id + ";" + name + ";" + getRecipesString();
    }
    
    /**
     * Checks its category to see if it is in the known ArrayList
     *
     * @return True if the element is known by the category or if the element is basic
     */
    public boolean isKnown()
    {
        return isBasic() || category != null && category.getElementById(id, true) != null;
    }
    
    /**
     * Creates an string that can be used to export the recipes to a safe file
     *
     * @return A valid export string
     */
    @NotNull
    public String getRecipesString()
    {
        StringBuilder output = new StringBuilder();
        // Appends the recipes together
        for (String recipe : recipes)
        {
            output.append(recipe).append(";");
        }
        return output.toString();
    }
    
    /**
     * Simple getter to learn if the element is basic
     *
     * @return True if the element is basic
     */
    public boolean isBasic()
    {
        return basic;
    }
    
    public void removeRecipe(String recipe)
    {
        // Remove the recipe from the recipes ArrayList
        for (int i = 0; i < recipes.size(); i++)
        {
            // If the recipe is the same: remove it
            if (recipes.get(i).equals(recipe))
            {
                recipes.remove(i);
                return;
            }
        }
    }
}
