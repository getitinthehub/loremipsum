import java.util.ArrayList;

/**
 * Created for elementally
 * <p>
 * Started on 23-5-2017
 *
 * @author Thomas
 */
public class Element
{
    private static int highestId = 0;
    
    private final int id;
    private String name;
    private ArrayList<String> quizedRecipes, knownRecipes, unknownRecipes;
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
        quizedRecipes = new ArrayList<>();
        knownRecipes = new ArrayList<>();
        unknownRecipes = new ArrayList<>();
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
     * Reads a line and interprets the data to a element
     *
     * @param line The line that should be read
     *
     * @return The element resulting in the reading
     * @throws ElementallyException When a line could not be a element
     */
    public static Element parseLine(String line) throws ElementallyException
    {
        String[] components = line.split(";");
        if (components.length < 6)
        {
            throw new ElementallyException("Invalid argument amount");
        }
        boolean basic = components[0].equals("b");
        if (!basic && !components[0].equals("k") && !components[0].equals("u"))
        {
            throw new ElementallyException("Element must start with b, k or u");
        }
        int id;
        try
        {
            id = Integer.parseInt(components[1]);
        }
        catch (NumberFormatException nfEx)
        {
            throw new ElementallyException("id must be a number");
        }
        String name = components[2];
        Element loaded = new Element(name, id, basic);
        int index = 4; // b;1;fire;q;START HERE;k;u;
        ArrayList<String> quizedRecipes = new ArrayList<>();
        ArrayList<String> knownRecipes = new ArrayList<>();
        ArrayList<String> unknownRecipes = new ArrayList<>();
        String recipe;
        for (; index < components.length && !(recipe = components[index]).equals("k"); index++)
        {
            quizedRecipes.add(recipe);
        }
        index++; // Skip the identifier
        for (; index < components.length && !(recipe = components[index]).equals("u"); index++)
        {
            knownRecipes.add(recipe);
        }
        index++; // Skip the identifier
        for (; index < components.length; index++)
        {
            unknownRecipes.add(components[index]);
        }
        loaded.quizedRecipes = quizedRecipes;
        loaded.knownRecipes = knownRecipes;
        loaded.unknownRecipes = unknownRecipes;
        return loaded;
    }
    
    /**
     * Simple getter for the category of this element
     *
     * @return The category of this element or null
     */
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
        unknownRecipes.add(toAdd);
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
     * Simple getter for the knownRecipes
     *
     * @return The knownRecipes of this instance
     */
    public ArrayList<String> getKnownRecipes()
    {
        return new ArrayList<>(knownRecipes);
    }
    
    /**
     * Adds the quized, known and unknown recipes in one arrayList
     *
     * @return An ArrayList with all the recipes
     */
    public ArrayList<String> getAllRecipes()
    {
        ArrayList<String> output = new ArrayList<>(unknownRecipes);
        output.addAll(knownRecipes);
        output.addAll(quizedRecipes);
        return output;
    }
    
    /**
     * Moves a recipe from known to quized
     *
     * @param recipe The recipe that got quized
     */
    public void gotQuized(String recipe)
    {
        move(recipe, knownRecipes, quizedRecipes);
    }
    
    /**
     * Moves a string from one ArrayList to another
     *
     * @param string The String that should be moved
     * @param from   The arrayList that the string is in
     * @param to     The arrayList that the string should go in
     */
    private void move(String string, ArrayList<String> from, ArrayList<String> to)
    {
        // Remove the string and add it to the destination
        for (int i = 0; i < from.size(); i++)
        {
            // If the String matches: remove the String and add it to the new arrayList
            if (from.get(i).equals(string))
            {
                from.remove(i);
                to.add(string);
                return;
            }
        }
    }
    
    /**
     * Makes a recipe quizable again
     * Starts with the last element and works it way back
     *
     * @param recipe The recipe that should be made available
     */
    public void quizCanceled(String recipe)
    {
        // Remove the string and add it to the destination
        // Starts from the back for optimization
        for (int i = quizedRecipes.size() - 1; i >= 0; i--)
        {
            // If the String matches: remove the String and add it to the new arrayList
            if (quizedRecipes.get(i).equals(recipe))
            {
                quizedRecipes.remove(i);
                knownRecipes.add(recipe);
                return;
            }
        }
    }
    
    /**
     * Learns a recipe
     *
     * @param recipe The recipe that should be learned
     */
    public void learnRecipe(String recipe)
    {
        move(recipe, unknownRecipes, knownRecipes);
    }
    
    /**
     * Unlearn all recipes
     */
    public void unLearnAll()
    {
        unknownRecipes.addAll(quizedRecipes);
        unknownRecipes.addAll(knownRecipes);
        quizedRecipes = new ArrayList<>();
        knownRecipes = new ArrayList<>();
    }
    
    /**
     * Removes a recipe from the element
     *
     * @param recipe The recipe that should be removed
     */
    public void removeRecipe(String recipe)
    {
        if (removeRecipeFrom(recipe, quizedRecipes)) return;
        if (removeRecipeFrom(recipe, knownRecipes)) return;
        removeRecipeFrom(recipe, unknownRecipes);
    }
    
    /**
     * Helper method for removing recipes
     *
     * @param recipe       The recipe that should be removed
     * @param knownRecipes The ArrayList it should be removed from
     *
     * @return True if the element was in the list
     */
    private boolean removeRecipeFrom(String recipe, ArrayList<String> knownRecipes)
    {
        // Remove the recipe from the knownRecipes ArrayList
        for (int i = 0; i < knownRecipes.size(); i++)
        {
            // If the recipe is the same: remove it
            if (knownRecipes.get(i).equals(recipe))
            {
                knownRecipes.remove(i);
                return true;
            }
        }
        return false;
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
    private String getRecipesString()
    {
        StringBuilder output = new StringBuilder();
        output.append("q;");
        for (String recipe : quizedRecipes)
        {
            output.append(recipe);
        }
        output.append("k;");
        for (String recipe : knownRecipes)
        {
            output.append(recipe).append(";");
        }
        output.append("u;");
        for (String recipe : unknownRecipes)
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
    
    public ArrayList<String> getUnknownRecipes()
    {
        return unknownRecipes;
    }
}
