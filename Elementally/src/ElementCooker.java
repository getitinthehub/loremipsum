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
    private static final String NO_NOTHING_ERROR = "Incorrect file format, nothing element must be present";
    private static final String ALL_COMBINATIONS_FILLED_ERROR = "All combinations are filled in";
    
    private Element nothing;
    private ArrayList<Category> categories;
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
        categories = new ArrayList<>();
        nothing = new Element(NOTHING_NAME, 0, true);
    }
    
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
            initGlobals();
            Element.resetCounter();
            
            Category fire = new Category("fire");
            Category water = new Category("water");
            Category earth = new Category("earth");
            Category air = new Category("air");
            
            fire.addElement(new Element("fire", 1, true));
            water.addElement(new Element("water", 2, true));
            earth.addElement(new Element("earth", 3, true));
            air.addElement(new Element("air", 4, true));
            
            categories.add(fire);
            categories.add(water);
            categories.add(earth);
            categories.add(air);
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
    public Element combine(Element element1, Element element2, boolean learn)
    {
        String key = getKey(element1.getId(), element2.getId());
        Element result = recipes.get(key);
        if (learn && result != null)
        {
            result.learnRecipe(key);
        }
        return result;
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
            if (previous.getAllRecipes().isEmpty() && !previous.isBasic())
            {
                remove(previous, true);
            }
        }
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
            for (String recipe : toRemove.getAllRecipes())
            {
                recipes.put(recipe, null);
            }
        }
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
        categories.remove(toDelete);
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
    
    public void loadDataFrom(String dataLine) throws ElementallyException
    {
        String[] data = dataLine.split("\n");
        int lineNumber = findNothingElement(data) + 1; // Throws ElementallyException
        Category lastCategory = null;
        while (lineNumber < data.length)
        {
            try
            {
                String[] components = data[lineNumber].split(";");
                if (components.length > 1)
                {
                    if (components[0].equals("c"))
                    {
                        lastCategory = new Category(components[1]);
                        addCategory(lastCategory);
                    }
                    else if (lastCategory != null)
                    {
                        Element loaded = Element.parseLine(data[lineNumber]);
                        lastCategory.addElement(loaded);
                        for (String recipe : loaded.getAllRecipes())
                        {
                            recipes.put(recipe, loaded);
                        }
                        // If the element is known: learn it
                        if (components[0].equals("k"))
                        {
                            lastCategory.learn(loaded);
                        }
                    }
                    // If there is no last category: throw an error
                    else
                    {
                        throw new ElementallyException("no category specified");
                    }
                }
                else
                {
                    throw new ElementallyException("incorrect format, must have at least 2 arguments");
                }
            }
            catch (ElementallyException eEx)
            {
                System.err.println("line" + (lineNumber + 1) + ": " + eEx.getMessage());
            }
            lineNumber++;
        }
    }
    
    private int findNothingElement(String[] data) throws ElementallyException
    {
        int lineNumber = 0;
        while (lineNumber < data.length)
        {
            String line = data[lineNumber];
            String[] components = line.split(";");
            // If this line contains the nothing element: add it and all the recipes
            if (components[0].equals(nothing.getName()))
            {
                try
                {
                    nothing = Element.parseLine("b;0;" + line);
                    return lineNumber;
                }
                catch (ElementallyException eEx)
                {
                    System.out.println(line + ": " + eEx.getMessage());
                }
            }
            lineNumber++;
        }
        throw new ElementallyException(NO_NOTHING_ERROR);
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
     * Data can only be saved in activities
     */
    public String getSaveString()
    {
        StringBuilder output = new StringBuilder();
        output.append(nothing.getName())
              .append(";")
              .append(nothing.getRecipesString())
              .append("\n");
        // Save all the categories and their elements
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
        return output.toString();
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
    public Element[] getEmptyCombination(Element first, boolean allowDuplicates) throws ElementallyException
    {
        // todo: only check half of the combinations
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
     * Calculates the known percentage.
     *
     * @return The percentage of elements that are known
     */
    public double getProgress()
    {
        double total = 0;
        double known = 0;
        // Count all the total elements and known elements
        for (Category category : categories)
        {
            total += category.getContaining().size();
            known += category.getKnown().size();
        }
        return known / total * 100;
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
    
    public Answer getQuizAnswer(boolean correct) throws ElementallyException
    {
        ArrayList<Category> knownCategories = getKnownCategories();
        int stopAtCat = (int) (knownCategories.size() * Math.random());
        int stopAtEle = (int) (knownCategories.get(stopAtCat).getKnown().size() * Math.random());
        int currentCat = stopAtCat;
        int currentEle = stopAtEle;
        boolean fullCircle = false;
        int maxCat = knownCategories.get(currentCat).getKnown().size();
        while (!fullCircle)
        {
            Element current = knownCategories.get(currentCat).getKnown().get(currentEle);
            ArrayList<String> answers = current.getKnownRecipes();
            if (answers.size() > 0)
            {
                ArrayList<String> recipes = current.getKnownRecipes();
                String answer = recipes.get((int) (Math.random() * recipes.size()));
                current.gotQuized(answer);
                String[] ids = answer.split(",");
                return new Answer(correct,
                                  new Element[]{getElementById(Integer.parseInt(ids[0]), true),
                                                getElementById(Integer.parseInt(ids[1]), true)},
                                  current);
            }
            if (++currentEle == maxCat)
            {
                currentEle = 0;
                currentCat = (currentCat + 1) % knownCategories.size();
                maxCat = knownCategories.get(currentCat).getKnown().size();
            }
            fullCircle = currentCat == stopAtCat && currentEle == stopAtEle;
        }
        throw new ElementallyException("No quizable elements");
    }
    
    private ArrayList<Category> getKnownCategories()
    {
        ArrayList<Category> known = new ArrayList<>();
        for (Category category : categories)
        {
            if (category.getKnown().size() > 0)
            {
                known.add(category);
            }
        }
        return known;
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
}
