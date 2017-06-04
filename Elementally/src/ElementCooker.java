import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created for elementally
 * <p>
 * Started on 23-5-2017
 *
 * @author Thomas
 */
public class ElementCooker
{
    public static final String NOTHING_NAME = "nothing";
    
    private static final ElementCooker instance = new ElementCooker();
    private static final String ALL_COMBINATIONS_FILLED_ERROR = "All combinations are filled in";
    private static final String NO_ARGUMENTS_ERROR = "Line must contain arguments";
    private static final String INVALID_ARGUMENT_AMOUNT_CATEGORY = "Category must have two arguments";
    private static final String NO_CATEGORY_SPECIFIED = "no category specified for element";
    private static final String NO_QUIZABLE_ELEMENTS = "No quizable elements";
    private static final String NO_NEXT_ELEMENT = "No element found";
    private static final String COULD_NOT_RESTART_ERROR = "Default state could not be loaded";
    
    private Element nothing;
    private ArrayList<Category> unknownCategories, knownCategories;
    private ArrayList<Category>[] allCategories;
    private HashMap<String, Element> recipes;
    
    /**
     * Constructor for the class
     */
    private ElementCooker()
    {
        initGlobals();
    }
    
    /**
     * Initializes the non static global variables
     */
    private void initGlobals()
    {
        recipes = new HashMap<>();
        unknownCategories = new ArrayList<>();
        knownCategories = new ArrayList<>();
        allCategories = new ArrayList[]{knownCategories, unknownCategories};
        nothing = new Element(NOTHING_NAME, 0, true);
    }
    
    /**
     * Standard getter for singletons
     *
     * @return The instance of this class
     */
    public static ElementCooker getInstance()
    {
        return instance;
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
            try
            {
                initGlobals();
                Element.resetCounter();
                loadDataFrom(StartState.startState); // Throws ElementallyException
            }
            catch (ElementallyException eEx)
            {
                System.err.println(COULD_NOT_RESTART_ERROR);
            }
        }
        // Else: unlearn all the elements
        else
        {
            // Go through every category and unlearn all elements
            for (Category category : knownCategories)
            {
                category.unlearnEverything();
            }
        }
    }
    
    /**
     * Loads all the data from a String
     *
     * @param dataLine The line with all the data
     *
     * @throws ElementallyException Thrown when the string does not contain the nothing element
     */
    public void loadDataFrom(String dataLine) throws ElementallyException
    {
        String[] data = dataLine.split("\n");
        int lineNumber = 0; // Throws ElementallyException
        Category lastCategory = null;
        // Import the data from all the lines
        while (lineNumber < data.length)
        {
            // Import data from a line
            try
            {
                String[] components = data[lineNumber].split(";");
                // If there are no arguments to read: throw an error
                if (components.length == 0)
                {
                    throw new ElementallyException(NO_ARGUMENTS_ERROR);
                }
                // If the line contains a category: add the category
                if (components[0].equals("c"))
                {
                    if (components.length != 2) throw new ElementallyException(INVALID_ARGUMENT_AMOUNT_CATEGORY);
                    lastCategory = new Category(components[1]);
                    addCategory(lastCategory);
                }
                // If there is a last category to add elements to: add it
                else
                {
                    Element loaded = Element.parseLine(data[lineNumber]); // Throws ElementallyException
                    // If a category is specified: add the element to that category
                    if (lastCategory != null)
                    {
                        lastCategory.addElement(loaded);
                        // If the element is known: learn it
                        if (components[0].equals("k"))
                        {
                            lastCategory.learn(loaded);
                        }
                    }
                    // Check if the element is the nothing element
                    else
                    {
                        // If the element is nothing: replace the element
                        if (loaded.getName().equals(NOTHING_NAME))
                        {
                            loaded = nothing;
                        }
                        // No category specified: throw an exception
                        else
                        {
                            throw new ElementallyException(NO_CATEGORY_SPECIFIED);
                        }
                    }
                    // Add all the recipes to the recipe map
                    for (String recipe : loaded.getAllRecipes())
                    {
                        recipes.put(recipe, loaded);
                    }
                }
            }
            // If a line has a incorrect format: let the player know
            catch (ElementallyException eEx)
            {
                System.err.println("line" + (lineNumber + 1) + ": " + eEx.getMessage());
            }
            lineNumber++;
        }
    }
    
    /**
     * Adds a category to this class
     *
     * @param category The category to add
     */
    public void addCategory(Category category)
    {
        // If there are known elements in the category: add it to known
        if (category.getKnown().size() > 0)
        {
            knownCategories.add(category);
        }
        // Else: add it to unknown
        else
        {
            unknownCategories.add(category);
        }
    }
    
    /**
     * Creates a combination of known and unknown categories
     *
     * @return An ArrayList with all the categories
     */
    public ArrayList<Category> getAllCategories()
    {
        ArrayList<Category> allCategories = new ArrayList<>(knownCategories);
        allCategories.addAll(unknownCategories);
        return allCategories;
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
            if (previous.getAllRecipes().isEmpty() && !previous.isBasic())
            {
                remove(previous, true);
            }
        }
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
     * Removes an element from its category and all its recipes
     *
     * @param toRemove     Element that needs to be removed
     * @param clearRecipes Indicates if the recipes resulting in this element should be forgotten as well
     */
    public void remove(Element toRemove, boolean clearRecipes)
    {
        assert toRemove != null : "null element";
        // Check all categories to remove the element
        for (ArrayList<Category> categoryKind : allCategories)
        {
            // Go through the categories and remove the element if it's in there
            for (Category category : categoryKind)
            {
                // If the element was removed: clean up where necessary
                if (category.remove(toRemove))
                {
                    // If the category is now empty: remove the category
                    if (category.getContaining().size() == 0)
                    {
                        categoryKind.remove(category);
                    }
                    // Remove the recipes when wanted
                    if (clearRecipes)
                    {
                        // Clear all the recipes
                        for (String recipe : toRemove.getAllRecipes())
                        {
                            recipes.put(recipe, null);
                        }
                    }
                    return;
                }
            }
        }
        assert false : "Element does not exist";
    }
    
    /**
     * Finds an element with a given name
     *
     * @param elementName The name of the element that needs to be found
     *
     * @return The element with the name or null if there is no element with that name
     */
    public Element getElementByName(String elementName)
    {
        if (elementName == null) return null;
        // Nothing is not in an category and will therefor be compared here
        if (nothing.getName().equals(elementName))
        {
            return nothing;
        }
        // Go through every category and returns the element if it's in there
        for (ArrayList<Category> categories : allCategories)
        {
            // Go through the categories and return the element
            for (Category category : categories)
            {
                Element found = category.getElementByName(elementName);
                // If the element is in this category: return it
                if (found != null)
                {
                    return found;
                }
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
        for (String recipe : toDelete.getAllRecipes())
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
        // remove the category
        for (ArrayList<Category> categories : allCategories)
        {
            categories.remove(toDelete);
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
        // Go through all categories and return the one with that name
        for (ArrayList<Category> categories : allCategories)
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
        }
        return null;
    }
    
    /**
     * @return the string that can be loaded later
     */
    public String getSaveString()
    {
        StringBuilder output = new StringBuilder(nothing.exportLine());
        // Save all the categories and their elements
        for (ArrayList<Category> categories : allCategories)
        {
            // Save the categories and their elements
            for (Category category : categories)
            {
                output.append("c;")
                      .append(category.getName())
                      .append("\n");
                // Save all the elements from each category
                for (Element element : category.getContaining())
                {
                    output.append(element.exportLine())
                          .append("\n");
                }
            }
        }
        return output.toString();
    }
    
    /**
     * Goes through every combination starting at a random position
     *
     * @param first           The first element of the combination, can be null
     * @param allowDuplicates True if elements can be the same
     *
     * @return An element combination that was not created yet
     * @throws ElementallyException When all the combinations are filled in
     */
    @SuppressWarnings("ConstantConditions")
    public Element[] getEmptyCombination(Element first, boolean allowDuplicates) throws ElementallyException
    {
        // todo: only check half of the combinations
        ArrayList<Category> categories = new ArrayList<>(unknownCategories);
        categories.addAll(knownCategories);
        //If there are elements to combine: combine them
        if (categories.size() != 0)
        {
            int stopAtCat1;
            int stopAtEle1;
            // If no element is chosen to start with: choose a random one
            if (first == null || first.getCategory() == null)
            {
                stopAtCat1 = (int) (categories.size() * Math.random());
                stopAtEle1 = (int) (categories.get(stopAtCat1).getContaining().size() * Math.random());
            }
            // If a element is chosen to start with: start with that element
            else
            {
                stopAtCat1 = categories.indexOf(first.getCategory());
                stopAtEle1 = first.getCategory().indexOf(first);
            }
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
    
    /**
     * Finds and returns a quizable element
     *
     * @param exclude The element that should not be returned, can be null
     *
     * @return A element that hasn't been asked before
     * @throws ElementallyException When there are no quizable elements
     */
    public Element[] getQuizAnswer(Element exclude) throws ElementallyException
    {
        int stopAtCat = (int) (knownCategories.size() * Math.random());
        int stopAtEle = (int) (knownCategories.get(stopAtCat).getKnown().size() * Math.random());
        int currentCat = stopAtCat;
        int currentEle = stopAtEle;
        boolean fullCircle = false;
        int maxCat = knownCategories.get(currentCat).getKnown().size();
        // Go through all the elements until one is quizable
        while (!fullCircle)
        {
            Element current = knownCategories.get(currentCat).getKnown().get(currentEle);
            ArrayList<String> answers = current.getKnownRecipes();
            // If there are known recipes that aren't quized yet: return a recipe
            if (!current.equals(exclude) && answers.size() > 0)
            {
                ArrayList<String> recipes = current.getKnownRecipes();
                String answer = recipes.get((int) (Math.random() * recipes.size()));
                current.gotQuized(answer);
                String[] ids = answer.split(",");
                return new Element[]{getElementById(Integer.parseInt(ids[0]), true),
                                     getElementById(Integer.parseInt(ids[1]), true)};
            }
            // If there are no elements left in the category: go to the next category
            if (++currentEle == maxCat)
            {
                currentEle = 0;
                currentCat = (currentCat + 1) % knownCategories.size();
                maxCat = knownCategories.get(currentCat).getKnown().size();
            }
            fullCircle = currentCat == stopAtCat && currentEle == stopAtEle;
        }
        throw new ElementallyException(NO_QUIZABLE_ELEMENTS);
    }
    
    /**
     * Finds an element with a given id
     *
     * @param elementId The id of the element that needs to be found
     * @param fromKnown True if the element should be gotten from the known ArrayList
     *
     * @return The element with the id or null if there is no element with that id
     */
    public Element getElementById(int elementId, boolean fromKnown)
    {
        // Nothing is not in an category and will therefor be compared here
        if (elementId == nothing.getId())
        {
            return nothing;
        }
        // Go through every category and returns the element if it's in there
        for (ArrayList<Category> categories : allCategories)
        {
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
        }
        return null;
    }
    
    /**
     * Generates an arrayList with all the known categories
     *
     * @return A arrayList with categories that have known elements in them
     */
    public ArrayList<Category> getKnownCategories()
    {
        return knownCategories;
    }
    
    /**
     * Goes through every recipe of every element and checks if it can be created using the known elements
     *
     * @return An element that can be created with the known elements
     * @throws ElementallyException When there are no elements that can be created
     */
    public Element getNextElement() throws ElementallyException
    {
        // If there are elements that are unknown: find a unknown element that can be created
        if (isOngoing())
        {
            ArrayList<Category>[] allCategories = new ArrayList[]{unknownCategories, knownCategories};
            // Go through the unknown categories and than the known categories to find a creatable element
            for (ArrayList<Category> categories : allCategories)
            {
                // Find the first unknown creatable element in a category
                for (Category category : categories)
                {
                    // Find the first creatable element
                    for (Element element : category.getUnknown())
                    {
                        // Go through every recipe and check if the ingredients are known
                        for (String s : element.getUnknownRecipes())
                        {
                            String[] recipe = s.split(",");
                            // If both ingredients are known: return the element
                            if (getElementById(Integer.parseInt(recipe[1]), true) != null &&
                                getElementById(Integer.parseInt(recipe[0]), true) != null)
                            {
                                return element;
                            }
                        }
                    }
                }
            }
        }
        throw new ElementallyException(NO_NEXT_ELEMENT);
    }
    
    /**
     * Helper method to check if there are still unknown elements
     *
     * @return True if there is at least 1 unknown element
     */
    private boolean isOngoing()
    {
        // If there are unknown categories: return true
        if (unknownCategories.size() > 0) return true;
        // Go through the categories
        for (Category category : knownCategories)
        {
            // If there are unknown elements: return true
            if (category.getContaining().size() > category.getKnown().size())
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculates the known percentage.
     *
     * @return The percentage of elements that are known
     */
    public double getProgress()
    {
        double total = 0;
        double known = 0;
        // Count all the unknown elements from unknown categories
        for (Category unknownCategory : unknownCategories)
        {
            total += unknownCategory.getContaining().size();
        }
        // Count all the total elements and known elements from known categories
        for (Category category : knownCategories)
        {
            total += category.getContaining().size();
            known += category.getKnown().size();
        }
        return known / total * 100;
    }
    
    /**
     * Makes a recipe quizable again
     *
     * @param answer The combination associated with the question
     */
    public void cancelQuiz(Element[] answer)
    {
        combine(answer[0], answer[1], false).quizCanceled(getKey(answer[0].getId(), answer[1].getId()));
    }
    
    /**
     * Combines two elements with each other and returns the result
     *
     * @param element1 The first element of the combination
     * @param element2 The second element of the combination
     * @param learn    True if the element should be learned when it's unlearned
     *
     * @return An element that this combination would result in or null if the combination is not defined before
     */
    public Element combine(Element element1, Element element2, boolean learn)
    {
        String key = getKey(element1.getId(), element2.getId());
        Element result = recipes.get(key);
        // If the result can and should be learned: learn it
        if (learn && result != null)
        {
            result.learnRecipe(key);
        }
        return result;
    }
}
