import com.sun.istack.internal.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Holds the result for recipes
 * <p>
 * Started on 13-4-2017<br>
 * Last changes made on 10-5-2017
 *
 * @author Thomas Holleman
 */
public class ElementCooker
{
    public static final String NOTHING_NAME = "nothing";
    public static final String NO_NOTHING_ERROR = "Incorrect file format, nothing element must be present";
    public static final String ALL_COMBINATIONS_FILLED_ERROR = "All combinations are filled in.";
    
    private Element nothing;
    private ArrayList<Category> categories;
    private HashMap<String, Element> recipes;
    
    /**
     * Constructor for the class
     */
    public ElementCooker()
    {
        initGlobals();
    }
    
    /**
     * Returns the game to the starting state with fire, water, earth and air.
     *
     * @param removeElements True if the existing elements should be removed, false if just forgotten
     */
    public void startState(boolean removeElements)
    {
        // If the elements should be removed: remove them and go back to the first four elements
        if (removeElements)
        {
            initGlobals();
            Element.resetCounter();
            String[] categories = {"fire", "water", "earth", "air"};
            String[] elements = {"fire", "water", "earth", "air"};
            // Add all the elements and categories
            for (int i = 0; i < categories.length; i++)
            {
                Category starter = new Category(categories[i]);
                starter.addElement(new Element(elements[i], i, true));
                addCategory(starter);
            }
        }
        // Else: unlearn all the elements
        else
        {
            // Go through every category and unlearn all elements
            for (Category category : categories)
            {
                category.unlearnEverything();
            }
        }
    }
    
    /**
     * Initializes the non static global variables
     */
    private void initGlobals()
    {
        recipes = new HashMap<>();
        categories = new ArrayList<>();
        nothing = new Element(NOTHING_NAME);
    }
    
    /**
     * Simple getter for the categories
     *
     * @return The categories in this object
     */
    public ArrayList<Category> getCategories()
    {
        return categories;
    }
    
    /**
     * Combines two elements with each other and returns the result
     *
     * @param element1 The first element of the combination
     * @param element2 The second element of the combination
     *
     * @return An element that this combination would result in or null if the combination is not defined before
     */
    @Nullable
    public Element combine(Element element1, Element element2)
    {
        return recipes.get(getKey(element1.getId(), element2.getId()));
    }
    
    /**
     * Gets the key for the HashMap
     *
     * @param elementId1 The id for the first element
     * @param elementId2 The id for the second element
     *
     * @return A key that can be used to get the Element from the HashMap
     */
    private String getKey(int elementId1, int elementId2)
    {
        int smallest = elementId1;
        int largest = elementId2;
        // If the ids are the wrong way round: switch them
        if (elementId1 > elementId2)
        {
            smallest = elementId2;
            largest = elementId1;
        }
        return smallest + "," + largest;
    }
    
    /**
     * Adds a recipe
     *
     * @param elementId1      The first id of the element that will make up the recipe
     * @param elementId2      The second id of the element that will make up the recipe
     * @param existingElement The result of the recipe
     */
    public void addRecipe(int elementId1, int elementId2, Element existingElement)
    {
        if (existingElement == null) return;
        String key = getKey(elementId1, elementId2);
        existingElement.addRecipe(key);
        Element previous = recipes.put(key, existingElement);
        // If there already was an element in that position: remove the element if that was the last recipe for it
        if (previous != null)
        {
            previous.removeRecipe(key);
            // Todo: Search for infinite loops
            // If there are no recipes left for that element and it's not basic: remove it
            if (previous.getRecipes().isEmpty() && previous.isBasic())
            {
                remove(previous, true);
            }
        }
    }
    
    /**
     * Finds an element with a given id
     *
     * @param elementId The id of the element that needs to be found
     * @param fromKnown True if the element should be gotten from the known ArrayList
     *
     * @return The element with the id or null if there is no element with that id
     */
    @Nullable
    public Element getElementById(int elementId, boolean fromKnown)
    {
        // Nothing is not in an category and will therefor be compared here
        if (elementId == 0)
        {
            return nothing;
        }
        // Go through every element and returns the element if it's in there
        for (Category category : categories)
        {
            Element found = category.getElementById(elementId, fromKnown);
            // If the element is found: return it
            if (found != null)
            {
                return found;
            }
        }
        return null;
    }
    
    /**
     * Finds an element with a given name
     *
     * @param elementName The name of the element that needs to be found
     *
     * @return The element with the name or null if there is no element with that name
     */
    @Nullable
    public Element getElementByName(String elementName)
    {
        if (elementName == null) return null;
        // Nothing is not in an category and will therefor be compared here
        if (nothing.getName().equals(elementName))
        {
            return nothing;
        }
        // Goes through every category and returns the element if it's in there
        for (Category category : categories)
        {
            Element found = category.getElementByName(elementName);
            // If the element is in this category: return it
            if (found != null)
            {
                return found;
            }
        }
        return null;
    }
    
    /**
     * Merges 2 elements into 1
     *
     * @param base     The element that needs to keep existing
     * @param toDelete The element of which the recipes will get moved to base and which will be deleted
     */
    public void merge(Element base, Element toDelete)
    {
        if (base == null || toDelete == null || base.equals(toDelete)) return;
        remove(toDelete, false);
        // Goes through every recipe and sets them to the base element
        for (String recipe : toDelete.getRecipes())
        {
            recipes.put(recipe, base);
            base.addRecipe(recipe);
        }
    }
    
    /**
     * Merges 2 categories into 1
     *
     * @param base     The category that needs to keep existing
     * @param toDelete The category of which the elements will get moved to base and which will be deleted
     */
    public void merge(Category base, Category toDelete)
    {
        if (base == null || toDelete == null || base.equals(toDelete)) return;
        // Add all the elements from the deleting category to the base category
        for (Element element : toDelete.getContaining())
        {
            base.addElement(element);
        }
        categories.remove(toDelete);
    }
    
    /**
     * Removes an element from its category and all its recipes
     *
     * @param toRemove     Element that needs to be removed
     * @param clearRecipes Indicates if the recipes resulting in this element should be forgotten as well
     */
    public void remove(Element toRemove, boolean clearRecipes)
    {
        if (toRemove == null) return;
        int i = 0;
        // Try to remove the element from every category until one actually removes something
        while (!categories.get(i).remove(toRemove))
        {
            // If the next removal attempt will throw an out of bounds exception: stop
            if (++i == categories.size())
            {
                return;
            }
        }
        // If the category is now empty: remove the category
        if (categories.get(i).getContaining().size() == 0)
        {
            categories.remove(i);
        }
        // If the recipes should be removed: remove them
        if (clearRecipes)
        {
            // Remove every recipe that will result into that element
            for (String recipe : toRemove.getRecipes())
            {
                recipes.put(recipe, null);
            }
        }
    }
    
    /**
     * Finds an category with a given name
     *
     * @param categoryName The name of the category that needs to be found
     *
     * @return The category with that name or null if there is no element with that name
     */
    public Category getCategoryByName(String categoryName)
    {
        // Go through every category and return the one with that name
        for (Category category : categories)
        {
            // If the category with that name is found: return it
            if (category.getName().equals(categoryName))
            {
                return category;
            }
        }
        return null;
    }
    
    /**
     * Adds a category to this class
     *
     * @param category The category to add
     */
    public void addCategory(Category category)
    {
        categories.add(category);
    }
    
    /**
     * Loads the safe data from a given file
     * <p>
     * A safe file must have a nothing element
     * All categories and elements that need to be loaded must be after this nothing element
     * All lines must follow the following formats:
     * <p>
     * Recipe: [int id],[int id]<br>
     * RecipeCollection: [recipe];[recipe]; etc...<br>
     * Nothing: nothing;[recipeCollection]<br>
     * Category: c;[String name]<br>
     * Known: 'b' if the element is basic, 'k' if the element is known, 'u' if the element is unknown
     * Element: [known];[int id];[String name];[RecipeCollection]
     *
     * @param location The location of the safe file
     *
     * @throws FileNotFoundException Thrown when the file could not be found or when the nothing element was not found
     */
    public void loadSafeFile(String location) throws FileNotFoundException
    {
        Scanner importer = new Scanner(new File(location)); // Throws FileNotFoundException
        int lineNumber;
        // Find the nothing element
        try
        {
            lineNumber = findNothingElement(importer);
        }
        // If the file does not contain the nothing element, it not the correct file: throw an error
        catch (ElementallyException eEx)
        {
            throw new FileNotFoundException(NO_NOTHING_ERROR);
        }
        Category lastCategory = null;
        // Parse all the remaining lines
        while (importer.hasNextLine())
        {
            // parse the element/category
            try
            {
                String[] components = importer.nextLine().split(";");
                // If the line has at least 1 line: add its data
                if (components.length > 1)
                {
                    // If the line is a category: add it
                    if (components[0].equals("c"))
                    {
                        lastCategory = new Category(components[1]);
                        addCategory(lastCategory);
                    }
                    // If it's not a category then it's element it should be added to the last category
                    else if (lastCategory != null)
                    {
                        // Add the element
                        try
                        {
                            Element loaded = new Element(components[2], Integer.parseInt(components[1]), components[0].equals("b"));
                            addRecipes(loaded, components, 3, lineNumber); // Throws ElementallyException
                            lastCategory.addElement(loaded);
                            // If the element is known: learn it
                            if (components[0].equals("k"))
                            {
                                lastCategory.learn(loaded);
                            }
                        }
                        // If the id is not a number: throw an error
                        catch (NumberFormatException nfEx)
                        {
                            throw new ElementallyException("line " + lineNumber + ": element id is not a number");
                        }
                    }
                    // If there is no last element: throw an error
                    else
                    {
                        throw new ElementallyException(String.format("line %d: no category specified", lineNumber));
                    }
                }
                // Else: throw an error
                else
                {
                    throw new ElementallyException("line" + lineNumber + ": incorrect format, must have at least 2 arguments");
                }
            }
            // Display the error to inform the user
            catch (ElementallyException eEx)
            {
                System.err.println("[LOAD ERROR]: " + eEx.getMessage());
            }
            lineNumber++;
        }
        importer.close();
    }
    
    /**
     * Must be the first method called after invoking the scanner in order to find the nothing element
     * nothing line must start with nothing;[recipes]
     * elements and categories before the nothing element will not be loaded
     *
     * @param reader The Scanner which will be used
     *
     * @return The amount of lines it took to find the nothing element
     */
    private int findNothingElement(Scanner reader) throws ElementallyException
    {
        int lineNumber = 1;
        // Goes through all the lines until it finds the nothing element
        while (reader.hasNextLine())
        {
            String[] components = reader.nextLine().split(";");
            // If this line contains the nothing element: add it and all the recipes
            if (components[0].equals(nothing.getName()))
            {
                addRecipes(nothing, components, 1, lineNumber);
                return ++lineNumber;
            }
            lineNumber++;
        }
        throw new ElementallyException("Nothing element could not be found");
    }
    
    /**
     * Helper method used for loading data
     * This method will add all the recipes to an element
     *
     * @param recipeFor  The element the recipes will make
     * @param recipes    The array with all the recipes, recipes must follow the format: [int],[int]
     * @param startAt    The index the method should start looking at for recipes
     * @param lineNumber Used for error throwing
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void addRecipes(Element recipeFor, String[] recipes, int startAt, int lineNumber) throws ElementallyException
    {
        if (recipes == null) throw new ElementallyException("recipes can not be null");
        // Test if all the components are correctly formatted
        for (int i = startAt; i < recipes.length; i++)
        {
            String[] idA = recipes[i].split(",");
            // If the element is of the correct length: test its component
            if (idA.length == 2)
            {
                // Make sure all elements are integers
                try
                {
                    Integer.parseInt(idA[0]);
                    Integer.parseInt(idA[1]);
                }
                // If a part of the recipe was not an integer: throw an exception
                catch (NumberFormatException nfEx)
                {
                    throw new ElementallyException(String.format("line %d: recipe must have 2 integers", lineNumber));
                }
            }
            // Else: throw an error
            else
            {
                throw new ElementallyException(String.format("line %d: recipe must have 2 arguments", lineNumber));
            }
        }
        // Add all the recipes
        for (int i = startAt; i < recipes.length; i++)
        {
            recipeFor.addRecipe(recipes[i]);
            this.recipes.put(recipes[i], recipeFor);
        }
    }
    
    /**
     * Saves all of the categories, elements and recipes into a safe file
     *
     * @param safeFile The location the file needs to be written to
     *
     * @throws FileNotFoundException When the file could not be found
     */
    public void save(String safeFile) throws FileNotFoundException
    {
        PrintWriter saver = new PrintWriter(new File(safeFile)); // Throws FileNotFoundException
        saver.println(nothing.getName() + ";" + nothing.getRecipesString());
        // Save all the categories and their elements
        for (Category category : categories)
        {
            saver.println("c;" + category.getName());
            // Save all the elements from each category
            for (Element element : category.getContaining())
            {
                saver.println(element.exportLine());
            }
        }
        saver.close();
    }
    
    /**
     * Goes through every combination starting at a random position
     *
     * @param allowDuplicates True if elements can be the same
     *
     * @return An element combination that was not created yet
     * @throws ElementallyException When all the combinations are filled in
     */
    @SuppressWarnings("ConstantConditions")
    public Element[] emptyCombination(boolean allowDuplicates) throws ElementallyException
    {
        // todo: only check half of the combinations
        // todo: check from known when applicable
        //If there are elements to combine: combine them
        if (categories.size() != 0)
        {
            int stopAtCat1 = (int) (categories.size() * Math.random());
            int stopAtEle1 = (int) (categories.get(stopAtCat1).getContaining().size() * Math.random());
            int currentCat1 = stopAtCat1;
            int currentEle1 = stopAtEle1;
            boolean fullCircle1 = false;
            int maxCat1 = categories.get(currentCat1).getContaining().size();
            // Go through every combination until an empty combination is found
            while (!fullCircle1)
            {
                Element ingredient1 = categories.get(currentCat1).getContaining().get(currentEle1);
                int stopAtCat2 = (int) (categories.size() * Math.random());
                int stopAtEle2 = (int) (categories.get(stopAtCat2).getContaining().size() * Math.random());
                int currentCat2 = stopAtCat2;
                int currentEle2 = stopAtEle2;
                boolean fullCircle2 = false;
                int maxCat2 = categories.get(currentCat2).getContaining().size();
                // Go through every element in combination with the first element
                while (!fullCircle2)
                {
                    // If duplicates are allowed or the combination is not a duplicate
                    if (allowDuplicates || currentCat1 != currentCat2 || currentEle1 != currentEle2)
                    {
                        Element ingredient2 = categories.get(currentCat2).getContaining().get(currentEle2);
                        Element generated = recipes.get(getKey(ingredient1.getId(), ingredient2.getId()));
                        // If the combination is not filled in yet: return it
                        if (generated == null)
                        {
                            return new Element[]{ingredient1, ingredient2};
                        }
                    }
                    // If the current element is at the end of the category: go to the next category
                    if (++currentEle2 == maxCat2)
                    {
                        currentEle2 = 0;
                        currentCat2 = (currentCat2 + 1) % categories.size();
                        maxCat2 = categories.get(currentCat2).getContaining().size();
                    }
                    fullCircle2 = currentCat2 == stopAtCat2 && currentEle2 == stopAtEle2;
                }
                // If the current element is at the end of the category: go to the next category
                if (++currentEle1 == maxCat1)
                {
                    currentEle1 = 0;
                    currentCat1 = (currentCat1 + 1) % categories.size();
                    maxCat1 = categories.get(currentCat1).getContaining().size();
                }
                fullCircle1 = currentCat1 == stopAtCat1 && currentEle1 == stopAtEle1;
            }
        }
        throw new ElementallyException(ALL_COMBINATIONS_FILLED_ERROR);
    }
    
    /**
     * Learns an element
     *
     * @param toLearn The element to learn
     */
    public void learn(Element toLearn)
    {
        if (toLearn == null) return;
        Category category = toLearn.getCategory();
        if (category == null) return;
        category.learn(toLearn);
    }
}
