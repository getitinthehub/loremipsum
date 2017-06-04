import java.util.ArrayList;

/**
 * Created for elementally
 * <p>
 * Started on 23-5-2017
 *
 * @author Thomas
 */
public class Category
{
    private String name;
    private ArrayList<Element> containing;
    private ArrayList<Element> known;
    
    /**
     * Constructor for the class
     *
     * @param name The name of this category
     */
    public Category(String name)
    {
        containing = new ArrayList<>();
        known = new ArrayList<>();
        this.name = name;
    }
    
    /**
     * Simple getter for the name
     *
     * @return The name for this category
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Simple setter for the name of this category
     *
     * @param name The new name of this category
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Simple getter for the all the elements within this category
     *
     * @return An ArrayList with all the elements
     */
    public ArrayList<Element> getContaining()
    {
        return containing;
    }
    
    /**
     * Simple getter for all the elements that are known by the player in this category
     *
     * @return An ArrayList with all the known elements
     */
    public ArrayList<Element> getKnown()
    {
        return known;
    }
    
    /**
     * Adds the element in the correct place in order to keep this collection in order<br>
     * Will not add elements with the same Id<br>
     * Will not learn elements
     *
     * @param toAdd The element to add
     */
    public void addElement(Element toAdd)
    {
        addElement(toAdd, false);
    }
    
    /**
     * Adds the element in the correct place in order to keep this collection in order<br>
     * If a element is known it will always add it to the known ArrayList.<br>
     * Will not add elements when an element already has the same id.
     *
     * @param toAdd   The element that needs to be added
     * @param toKnown True if the element should be added to the known ArrayList
     */
    private void addElement(Element toAdd, boolean toKnown)
    {
        if (toAdd == null) return;
        ArrayList<Element> addTo = toKnown ? known : containing;
        // If the element is known but it is not currently added to known: add it to known
        if (!toKnown && toAdd.isKnown())
        {
            addElement(toAdd, true);
        }
        // If the category was unknown before but not anymore: learn the category
        if (toKnown && known.size() == 0)
        {
            ElementCooker.getInstance().learn(this);
        }
        toAdd.setCategory(this);
        int id = toAdd.getId();
        int largest = addTo.size() - 1;
        // If the element is larger than the largest element in this category: add it to the end
        if (addTo.isEmpty() || id > addTo.get(largest).getId())
        {
            addTo.add(toAdd);
            return;
        }
        if (addTo.get(largest).getId() == id) return;
        int smallest = 0;
        // If the element is smaller than the smallest element in this category: add it to the start
        if (id < addTo.get(smallest).getId())
        {
            addTo.add(0, toAdd);
            return;
        }
        if (addTo.get(smallest).getId() == id) return;
        // find the position the element needs placed at using a binary method
        while (smallest + 1 != largest)
        {
            int middle = (smallest + largest) / 2;
            int middleId = addTo.get(middle).getId();
            // If the element is smaller than the middle: move the highest limit
            if (id < middleId)
            {
                largest = middle;
            }
            // If the element already is in the category: don't add it
            else if (id == middleId)
            {
                return;
            }
            // Else: Move the smallest limit
            else
            {
                smallest = middle;
            }
        }
        addTo.add(largest, toAdd);
    }
    
    /**
     * Searches through the category to find an element with a given id
     *
     * @param elementId The id of the element that needs to be found
     * @param fromKnown True if the element should be looked for in the known ArrayList
     *
     * @return The element with the id or null if no such element could be found
     */
    public Element getElementById(int elementId, boolean fromKnown)
    {
        ArrayList<Element> from = fromKnown ? known : containing;
        int largest = from.size() - 1;
        // If the element is larger than the largest element in this category: return null
        if (from.isEmpty() || elementId > from.get(largest).getId())
        {
            return null;
        }
        int smallest = 0;
        // If the element is smaller than the smallest element in this category: return null
        if (elementId < from.get(smallest).getId())
        {
            return null;
        }
        // Search the element using a binary method
        while (smallest <= largest)
        {
            int middle = (smallest + largest) / 2;
            Element middleElement = from.get(middle);
            // If the element is smaller than the middle: move the highest limit
            if (elementId < middleElement.getId())
            {
                largest = middle - 1;
            }
            // If the element is found: remove the element and return true
            else if (elementId == middleElement.getId())
            {
                return middleElement;
            }
            // Else: Move the smallest limit
            else
            {
                smallest = middle + 1;
            }
        }
        return null;
    }
    
    /**
     * Searches through the category to find the element with a given name
     *
     * @param elementName The name of the element that needs to be found
     *
     * @return The element with the name or null if no such element could be found
     */
    public Element getElementByName(String elementName)
    {
        if (elementName == null) return null;
        // Go through every element in this category and return the element if it is in here
        for (Element element : containing)
        {
            // If an element is found with the same name: return it
            if (element.getName().equals(elementName))
            {
                return element;
            }
        }
        return null;
    }
    
    public int indexOf(Element element)
    {
        if (element == null) return -1;
        ArrayList<Element> from = containing;
        int elementId = element.getId();
        int largest = from.size() - 1;
        // If the element is larger than the largest element in this category: return -1
        if (from.isEmpty() || elementId > from.get(largest).getId())
        {
            return -1;
        }
        int smallest = 0;
        // If the element is smaller than the smallest element in this category: return -1
        if (elementId < from.get(smallest).getId())
        {
            return -1;
        }
        // Search the element using a binary method
        while (smallest <= largest)
        {
            int middle = (smallest + largest) / 2;
            Element middleElement = from.get(middle);
            // If the element is smaller than the middle: move the highest limit
            if (elementId < middleElement.getId())
            {
                largest = middle - 1;
            }
            // If the element is found: remove the element and return true
            else if (elementId == middleElement.getId())
            {
                return middle;
            }
            // Else: Move the smallest limit
            else
            {
                smallest = middle + 1;
            }
        }
        return -1;
    }
    
    /**
     * Adds a element to know ArrayList
     *
     * @param toLearn The element to learn
     */
    public void learn(Element toLearn)
    {
        // If the element is known to this category: learn the element
        if (getElementById(toLearn.getId(), false) != null)
        {
            addElement(toLearn, true);
        }
    }
    
    /**
     * toString of this class.
     * It is recommended that getName() is used for getting the name
     *
     * @return The name of this category
     */
    @Override
    public String toString()
    {
        return name;
    }
    
    /**
     * Removes an element from this category
     *
     * @param toRemove The element that needs to be removed
     *
     * @return True if the element could be found and removed
     */
    public boolean remove(Element toRemove)
    {
        if (toRemove == null) return false;
        int elementId = toRemove.getId();
        int largest = containing.size() - 1;
        // If the element is larger than the largest element in this category: return false
        if (containing.isEmpty() || elementId > containing.get(largest).getId())
        {
            return false;
        }
        int smallest = 0;
        // If the element is smaller than the smallest element in this category: return false
        if (elementId < containing.get(smallest).getId())
        {
            return false;
        }
        // Use binary search to find the element if it is in here
        while (smallest <= largest)
        {
            int middle = (smallest + largest) / 2;
            Element middleElement = containing.get(middle);
            // If the element is smaller than the middle: move the highest limit
            if (elementId < middleElement.getId())
            {
                largest = --middle;
            }
            // If the element is found: remove the element and return true
            else if (elementId == middleElement.getId())
            {
                containing.remove(middle);
                return true;
            }
            // Else: Move the smallest limit
            else
            {
                smallest = ++middle;
            }
        }
        return false;
    }
    
    /**
     * Resets the known ArrayList to the starting state
     */
    public void unlearnEverything()
    {
        ArrayList<Element> basic = new ArrayList<>();
        // Add all the basic elements to the ArrayList
        for (Element element : known)
        {
            element.unLearnAll();
            // If the element is basic: add it
            if (element.isBasic())
            {
                basic.add(element);
            }
        }
        known = new ArrayList<>();
        // learn all the basic elements again
        for (Element element : basic)
        {
            addElement(element);
        }
        // If this is now unknown: unlearn
        if (known.size() == 0)
        {
            ElementCooker.getInstance().unlearn(this);
        }
    }
    
    public ArrayList<Element> getUnknown()
    {
        ArrayList<Element> unknown = new ArrayList<>();
        int index = 0;
        while (containing.size() - known.size() - unknown.size() > 0)
        {
            Element toAdd = containing.get(index);
            if (!known.contains(toAdd))
            {
                unknown.add(toAdd);
            }
            index++;
        }
        return unknown;
    }
}
