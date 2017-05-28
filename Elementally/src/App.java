import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Prints the menu<br>
 * Takes the input from the player and passes it to the provider<br>
 * ensures the player can't combine two elements with the same id<br>
 * creates new elements and categories<br>
 * <p>
 * Started on 13-4-2017
 * <p>
 *
 * @author Thomas Holleman
 */
public class App
{
    private boolean allowDuplicates, running, editMode, showMenu;
    private ElementCooker game;
    private Scanner userInput;
    private String safeFileLocation;
    private Command[] commands;
    
    /**
     * Sets up the variables that can be changed in the main method
     */
    private App()
    {
        allowDuplicates = false;
        editMode = false;
    }
    
    /**
     * Main method for the Elementally game.
     * Give additional arguments to change settings for the game:<br>
     * allowDuplicates to allow elements to combine with themselves<br>
     * editMode to allow the editing and adding of recipes, elements and categories
     *
     * @param args The arguments for the setup
     */
    public static void main(String[] args)
    {
//        args = new String[]{"editMode"};
        App toRun = new App();
        // Changes all the settings according to the arguments
        for (String arg : args)
        {
            // Change booleans when the argument says so
            switch (arg)
            {
                // Allows 2 of the same elements to be combined
                case "allowDuplicates":
                    toRun.allowDuplicates = true;
                    break;
                // Allows the player to add and edit recipes, elements and categories
                case "editMode":
                    toRun.editMode = true;
                    break;
            }
        }
        toRun.run();
    }
    
    /**
     * Starts the game
     */
    private void run()
    {
        initGlobals();
        loadSafeFile();
        // Keep playing until the player wants to quit
        while (running)
        {
            if (showMenu)
            {
                printMenu();
                System.out.println();
            }
            showMenu = true;
            Element[] toCombine = askCombination();
            // If a combination was chosen: combine them
            if (toCombine != null)
            {
                combine(toCombine[0], toCombine[1]);
            }
            // Else if more will be printed it should be separated: print an empty line
            else if (running)
            {
                System.out.println();
            }
        }
    }
    
    /**
     * Loads the safe file from the safe file location
     */
    private void loadSafeFile()
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            Scanner loader = new Scanner(new File(safeFileLocation));
            while (loader.hasNextLine())
            {
                sb.append(loader.nextLine()).append("\n");
            }
            game.loadDataFrom(sb.toString());
        }
        catch (FileNotFoundException | ElementallyException e)
        {
            System.out.println("No previous safe file found, setting to starting state.");
            game.startState(true);
        }
    }
    
    /**
     * initializes the global variables of this class
     */
    private void initGlobals()
    {
        running = true;
        showMenu = true;
        game = ElementCooker.getInstance();
        safeFileLocation = "src\\SafeFile";
        userInput = new Scanner(System.in);
        setCommands();
    }
    
    /**
     * Sets the commands that can be used during the game.
     */
    private void setCommands()
    {
        Command save = new Command("save");
        save.setCode((args) ->
                     {
                         String saveString = game.getSaveString();
                         try
                         {
                             PrintWriter saver = new PrintWriter(new File(safeFileLocation));
                             saver.print(saveString);
                             saver.close();
                             System.out.println("game saved successfully");
                         }
                         catch (FileNotFoundException e)
                         {
                             System.out.println("game could not be saved, copy this in the safe file" +
                                                "\n" + saveString);
                         }
                         showMenu = false;
                         return null;
                     });
        Command exit = new Command("exit");
        exit.setCode((args) ->
                     {
                         running = !confirm();
                         return null;
                     });
        Command reset = new Command("reset");
        reset.setCode((args) ->
                      {
                          game.startState(editMode);
                          return null;
                      });
        Command cancel = new Command("cancel");
        // If the player should not edit any elements: don't add those commands
        if (!editMode)
        {
            Command quiz = new Command("quiz");
            quiz.setCode(args ->
                         {
                             askQuizQuestion();
                             return null;
                         });
            commands = new Command[]{save, quiz, exit, reset, cancel};
        }
        // Else: add all commands
        else
        {
            Command random = new Command("random", "");
            random.setCode((args) ->
                           {
                               // Find an empty combination
                               try
                               {
                                   Element startWith = null;
                                   if (args.length == 2)
                                   {
                                       startWith = parseElement(args[1]); // Throws NumberFormatException
                                   }
                                   return game.getEmptyCombination(startWith, allowDuplicates);
                               }
                               // If all combinations are filled in: inform the user
                               catch (ElementallyException eEx)
                               {
                                   System.out.println(eEx.getMessage());
                               }
                               catch (NumberFormatException nfEx)
                               {
                                   System.out.println("The correct format for renaming is: " + random.getName() + " [id]\n" + "or: " + random.getName() + "\nor an empty line");
                               }
                               return null;
                           });
            Command rename = new Command("rename");
            rename.setCode((args) ->
                           {
                               // If the command has the correct argument amount: rename an element
                               if (args.length == 3)
                               {
                                   renameElement(args[1], args[2]);
                               }
                               // Else: Inform the player
                               else
                               {
                                   System.out.println("The correct format for renaming is: " + rename.getName() + " [id] [newName]");
                               }
                               return null;
                           });
            Command move = new Command("move");
            move.setCode((args) ->
                         {
                             // If the command has the correct argument amount: rename an element
                             if (args.length == 3)
                             {
                                 moveElement(args[1], args[2]);
                             }
                             // Else: Inform the player
                             else
                             {
                                 System.out.println("The correct format for moving is: " + move.getName() + " [id] [categoryName]");
                             }
                             return null;
                         });
            Command renameCat = new Command("renameCat");
            renameCat.setCode((args) ->
                              {
                                  // If the command has the correct argument amount: rename an element
                                  if (args.length == 3)
                                  {
                                      renameCategory(args[1], args[2]);
                                  }
                                  // Else: Inform the player
                                  else
                                  {
                                      System.out.println("The correct format for renaming categories is: " + renameCat.getName() + " [categoryName] [newName]");
                                  }
                                  return null;
                              });
            Command remove = new Command("remove");
            remove.setCode((args) ->
                           {
                               // If the command has the correct argument amount: rename an element
                               if (args.length == 2)
                               {
                                   // Remove the given element
                                   try
                                   {
                                       removeElement(parseElement(args[1]));
                                   }
                                   // If the id was not a number: inform the player
                                   catch (NumberFormatException nfEx)
                                   {
                                       System.out.println("Please enter a valid id next time");
                                   }
                               }
                               // Else: Inform the player
                               else
                               {
                                   System.out.println("The correct format for removing elements is: " + remove.getName() + " [elementId]");
                               }
                               return null;
                           });
            Command editRecipe = new Command("editRecipe");
            editRecipe.setCode((args ->
            {
                // If the correct amount of arguments is given: combine the two elements
                if (args.length == 3)
                {
                    // Combine the two given elements
                    try
                    {
                        Element element1 = parseElement(args[1]); //Throws NumberFormatException
                        int lastId = allowDuplicates || element1 == null ? -1 : element1.getId();
                        Element element2 = parseElement(args[2], lastId); //Throws NumberFormatException
                        if (element1 == null || element2 == null) return null;
                        System.out.printf("%s and %s creates", element1.getName(), element2.getName());
                        createElement(element1, element2);
                    }
                    catch (NumberFormatException ignored)
                    {
                        System.out.println("Element id must be a integer");
                    }
                }
                // Else: inform the user of the correct format
                else
                {
                    System.out.println("The correct format for editing recipes is: " + editRecipe.getName() + " [elementId] [elementId]");
                }
                return null;
            }));
            commands = new Command[]{save, exit, reset, random, rename, move, renameCat, remove, editRecipe, cancel};
        }
    }
    
    private void askQuizQuestion()
    {
        try
        {
            Answer[] choices = new Answer[4];
            Answer correct = game.getQuizAnswer(true);
            choices[0] = correct;
            for (int i = 1; i < 4; i++)
            {
                choices[i] = game.getQuizAnswer(false);
            }
            randomize(choices);
            System.out.println("Which combination creates " + correct.getResult().getName());
            for (int i = 0; i < choices.length; i++)
            {
                System.out.println((i + 1) + choices[i].toString());
            }
            int answer = -1;
            do
            {
                try
                {
                    System.out.print("Answer: ");
                    answer = Integer.parseInt(userInput.nextLine());
                }
                catch (NumberFormatException ignored)
                {
                }
            }
            while (answer < 1 || answer > choices.length);
            if (choices[answer - 1].isCorrect())
            {
                System.out.println("Correct");
            }
            else
            {
                Answer chosen = choices[answer - 1];
                System.out.println("Incorrect, " + chosen + " creates " + chosen.getResult() +
                                   "\nThe correct answer was " + correct);
                
            }
        }
        catch (ElementallyException eEx)
        {
            System.out.println("There are no questions at the moment");
        }
    }
    
    private void randomize(Answer[] toRandomize)
    {
        for (int i = 0; i < toRandomize.length; i++)
        {
            int switchWith = (int) (Math.random() * toRandomize.length);
            Answer switcher = toRandomize[i];
            toRandomize[i] = toRandomize[switchWith];
            toRandomize[switchWith] = switcher;
        }
    }
    
    private Element[] randomCombinationFor(Element element)
    {
        assert element != null;
        ArrayList<String> possibleCombinations = element.getKnownRecipes();
        if (possibleCombinations.size() < 1) return null;
        String[] correctAnswerKey = possibleCombinations.get((int) (Math.random() * possibleCombinations.size())).split(",");
        return new Element[]{parseElement(correctAnswerKey[0]), parseElement(correctAnswerKey[1])};
    }
    
    /**
     * Tries to get an element with the id given by the player
     *
     * @param input The id of the element
     *
     * @return The element found with the id or null when no element is found
     * @throws NumberFormatException When the input is not a integer
     */
    private Element parseElement(String input) throws NumberFormatException
    {
        return parseElement(input, -1);
    }
    
    /**
     * Tries to get an element with the id given by the player
     *
     * @param input           The id of the element
     * @param previousElement The element that is not allowed to be filled in
     *
     * @return The element found with the id or null when no element is found
     * @throws NumberFormatException When the input is not a integer
     */
    private Element parseElement(String input, int previousElement) throws NumberFormatException
    {
        Element chosen = game.getElementById(Integer.parseInt(input), !editMode); //Throws NumberFormatException
        // If the element does not exist: inform the player.
        if (chosen == null)
        {
            System.out.println("an element does not exist with that number");
        }
        // If the element is already asked before: inform the player and invalidate the input
        else if (chosen.getId() == previousElement)
        {
            System.out.println("Elements can not be the same");
            chosen = null;
        }
        return chosen;
    }
    
    /**
     * Asks the player for two ids to combine.
     * If the player fills in an word it will be executed as an command
     *
     * @return An element combination with a length of 2 or null if an command does not create an combination.
     */
    private Element[] askCombination()
    {
        String input = "";
        // Ask the player for two elements and returns the result
        try
        {
            Element[] combination = new Element[2];
            // Asks the user for two elements
            for (int i = 0; i < combination.length; i++)
            {
                int previous;
                // If there is no previous element or it doesn't matter: set previous to -1
                if (combination[0] == null || allowDuplicates)
                {
                    previous = -1;
                }
                // Else: set previous to the previous elements id
                else
                {
                    previous = combination[0].getId();
                }
                // Keeps asking for a id until one is valid
                while (combination[i] == null)
                {
                    System.out.printf("Element %d: ", i + 1);
                    input = userInput.nextLine();
                    combination[i] = parseElement(input, previous);
                }
            }
            return combination;
        }
        // If a word is given instead of an integer: execute it as an command
        catch (NumberFormatException nfEx)
        {
            return executeCommand(input);
        }
    }
    
    /**
     * Executes the input as a command
     *
     * @param command The input from the player
     *
     * @return An combination when a command returns such a thing or null when this does not happen
     */
    private Element[] executeCommand(String command)
    {
        // If there are commands to look through: execute commands
        if (commands.length > 0)
        {
            String[] args = command.split(" ");
            // Go through all the commands and execute the one that should be
            for (Command possibleCommand : commands)
            {
                // If the command should be executed: execute it
                if (possibleCommand.shouldExecuteAt(args[0]))
                {
                    return possibleCommand.execute(args);
                }
            }
            System.out.print("Unknown command, available commands are: " + commands[0].getName());
            // If there are more commands to print: print them
            if (commands.length > 1)
            {
                // Print all the commands between the outer commands
                for (int i = 1; i < commands.length - 1; i++)
                {
                    System.out.print(", " + commands[i].getName());
                }
                System.out.println(" and " + commands[commands.length - 1].getName());
            }
        }
        return null;
    }
    
    /**
     * Removes an element and the recipes resulting in the element.
     *
     * @param toRemove The element to remove
     */
    private void removeElement(Element toRemove)
    {
        game.remove(toRemove, true);
    }
    
    /**
     * Renames an element.
     * If the new name is already taken this will ask the player if they want to merge the two elements.
     *
     * @param elementNumber The id of the element that should be renamed
     * @param newName       The new name of the element
     */
    private void renameElement(String elementNumber, String newName)
    {
        // Rename an element
        try
        {
            if (newName == null) return;
            // If the new name is empty: assume it should be the nothing element
            if (newName.trim().isEmpty()) newName = ElementCooker.NOTHING_NAME;
            Element toRename = parseElement(elementNumber); //Throws NumberFormatException
            if (toRename == null) return;
            Element existingElement = game.getElementByName(newName);
            // If the element name is not taken yet: rename the element
            if (existingElement == null)
            {
                toRename.setName(newName);
            }
            // Else: merge the two elements
            else
            {
                System.out.println(toRename.getName() + " and " + newName + " will be merged into one element.");
                // If the player confirms they want to merge the elements: merge them
                if (confirm())
                {
                    game.merge(existingElement, toRename);
                }
            }
        }
        // If the player filled in a word instead of an id: inform the player of this
        catch (NumberFormatException nfEx)
        {
            System.out.println("Please fill in a valid id next time.");
        }
    }
    
    /**
     * Moves an element from its current category to a new one.
     *
     * @param elementNumber The id of the element that should be moved
     * @param category      The category the element should be moved to
     */
    private void moveElement(String elementNumber, String category)
    {
        // Move an element to a category
        try
        {
            Element toMove = parseElement(elementNumber); // Throws NumberFormatException
            if (toMove == null) return;
            Category moveTo = game.getCategoryByName(category);
            // If the category didn't exist yet: create it
            if (moveTo == null)
            {
                moveTo = new Category(category);
            }
            game.remove(toMove, false);
            moveTo.addElement(toMove);
        }
        // If the player filled in a word instead of an id: inform the player of this
        catch (NumberFormatException nfEx)
        {
            System.out.println("Please fill in a valid id next time");
        }
    }
    
    /**
     * Renames a category.
     * If a the new name is already taken this will ask the player if they want to merge the two categories.
     *
     * @param currentName The current name of the category
     * @param newName     The new name of the category
     */
    private void renameCategory(String currentName, String newName)
    {
        if (newName == null) return;
        // If the new name is empty: inform the user and return
        if (newName.trim().isEmpty())
        {
            System.out.println("Category must have a new name");
            return;
        }
        Category toRename = game.getCategoryByName(currentName);
        // If the category does not exist: inform the player and return
        if (toRename == null)
        {
            System.out.println(currentName + " is a unknown category.");
            return;
        }
        Category existingCategory = game.getCategoryByName(newName);
        // If the category name is not taken yet: rename the category
        if (existingCategory == null)
        {
            toRename.setName(newName);
        }
        // Else: merge the two categories into one
        else
        {
            System.out.println(currentName + " and " + newName + " will be merged into one category.");
            // If the player really wants to merge the two categories: merge them
            if (confirm())
            {
                game.merge(existingCategory, toRename);
            }
        }
    }
    
    /**
     * Combines two elements into their result
     *
     * @param element1 The first element of the combination
     * @param element2 The second element of the combination
     */
    private void combine(Element element1, Element element2)
    {
        Element creates = game.combine(element1, element2, !editMode);
        System.out.printf("%s and %s creates", element1.getName(), element2.getName());
        // If the combination is known: print the result
        if (creates != null)
        {
            System.out.println(" " + creates.getName());
            game.learn(creates);
        }
        // Else if the player can create combinations: prompt the player for what it should create
        else if (editMode)
        {
            createElement(element1, element2);
        }
        // Else: print that it prints nothing
        else
        {
            System.out.println(" " + ElementCooker.NOTHING_NAME);
        }
        System.out.println();
    }
    
    /**
     * Creates the element that comes from a combination
     *
     * @param element1 The first element from the combination
     * @param element2 The second element from the combination
     */
    private void createElement(Element element1, Element element2)
    {
        if (element1 == null || element2 == null) return;
        System.out.print(": ");
        String elementName = userInput.nextLine();
        if (elementName.trim().isEmpty()) elementName = ElementCooker.NOTHING_NAME;
        Element creates = game.getElementByName(elementName);
        // If the element is not known yet: create it
        if (creates == null)
        {
            creates = new Element(elementName);
            String categoryName = "";
            // Make sure the category name is not empty
            while (categoryName.trim().isEmpty())
            {
                System.out.print("Category: ");
                categoryName = userInput.nextLine();
            }
            Category category = game.getCategoryByName(categoryName);
            // If the category is not known yet: create it
            if (category == null)
            {
                category = new Category(categoryName);
                game.addCategory(category);
            }
            category.addElement(creates);
        }
        game.addRecipe(element1.getId(), element2.getId(), creates);
    }
    
    /**
     * Print the categories and their elements
     */
    private void printMenu()
    {
        ArrayList<Category> categories = game.getCategories();
        // If all elements should be shown: show them all
        if (editMode)
        {
            // Print the categories and their elements
            for (Category category : categories)
            {
                ArrayList<Element> containing = category.getContaining();
                printCategory(category, containing);
            }
        }
        // Else: show only the known elements
        else
        {
            System.out.printf("Progress: %.1f%% %n", game.getProgress());
            // Print the categories and their elements
            for (Category category : categories)
            {
                ArrayList<Element> known = category.getKnown();
                printCategory(category, known);
            }
        }
    }
    
    /**
     * Prints a category and all its elements
     *
     * @param category   The category to display
     * @param containing The ArrayList that should be shown from the category
     */
    private void printCategory(Category category, ArrayList<Element> containing)
    {
        // If the category is not empty: display it
        if (!containing.isEmpty())
        {
            System.out.print(category.getName() + ":");
            // Print the elements from this category
            for (Element element : containing)
            {
                System.out.print(" " + element);
            }
            System.out.println();
        }
    }
    
    /**
     * Prints a confirm message and prompts the player for a answer
     *
     * @return True if the player gave an answer that starts with an y
     */
    private boolean confirm()
    {
        System.out.print("Continue? (Y/N) ");
        return userInput.nextLine().toLowerCase().startsWith("y");
    }
}
