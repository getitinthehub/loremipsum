import org.jetbrains.annotations.Nullable;

/**
 * Class to tie code to certain strings to execute at a later moment
 * <p>
 * Started on 7-5-2017<br>
 * Last changes made on 10-5-2017
 *
 * @author Thomas Holleman
 */
public class Command
{
    private executable code;
    private String[] executeAt;
    private String nameExecutor;
    
    /**
     * Constructor for the command
     *
     * @param nameExecutor The name of the command that will also execute this command
     * @param executeAt    The strings that should call this command when given
     */
    public Command(String nameExecutor, String... executeAt)
    {
        assert nameExecutor != null : "null name";
        this.nameExecutor = nameExecutor;
        this.executeAt = executeAt;
        code = null;
    }
    
    /**
     * @return The first string given in the constructor
     */
    @Nullable
    public String getName()
    {
        return nameExecutor;
    }
    
    /**
     * Tests if the command should execute when the given string is called
     *
     * @param command The string in question
     *
     * @return True if the command should be executed
     */
    public boolean shouldExecuteAt(String command)
    {
        // If the command is the name of the command: return true
        if (nameExecutor.equals(command))
        {
            return true;
        }
        // Go through every execute command and compare it to the argument
        for (String synonym : executeAt)
        {
            // If the argument is the same as the command: return true
            if (synonym.equals(command))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Set the code for this command
     *
     * @param code The code that should execute when this command is called
     */
    public void setCode(executable code)
    {
        this.code = code;
    }
    
    /**
     * Execute the code tied to this command.
     *
     * @param args The arguments to this command
     *
     * @return A combination or null if no combination is made
     */
    @Nullable
    public Element[] execute(String[] args)
    {
        // If code is specified: execute it
        if (code != null)
        {
            return code.execute(args);
        }
        return null;
    }
    
    /**
     * Simple interface to add code to commands
     */
    public interface executable
    {
        /**
         * Execute the code of the command
         *
         * @param args The arguments for the command
         *
         * @return An combination or null if nothing is created
         */
        @Nullable Element[] execute(String[] args);
    }
}
