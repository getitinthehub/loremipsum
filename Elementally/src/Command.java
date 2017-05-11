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
    
    /**
     * Constructor for the command
     *
     * @param executeAt The strings that should call this command when given
     */
    public Command(String... executeAt)
    {
        this.executeAt = executeAt != null ? executeAt : new String[]{null};
        code = null;
    }
    
    /**
     * @return The first string given in the constructor
     */
    @Nullable
    public String getName()
    {
        if (executeAt.length == 0) return null;
        return executeAt[0];
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
