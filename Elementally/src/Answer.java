import java.util.Arrays;

/**
 * Created for elementally
 * <p>
 * Started on 24-5-2017
 *
 * @author Thomas
 */
public class Answer
{
    private boolean correct;
    private Element result;
    private Element[] recipe;
    
    public Answer(boolean correct, Element[] recipe, Element result)
    {
        assert recipe != null;
        assert recipe.length == 2;
        this.result = result;
        this.correct = correct;
        this.recipe = recipe;
    }
    
    @Override
    public String toString()
    {
        return recipe[0] + " + " + recipe[1];
    }
    
    public Element getResult()
    {
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Answer))
        {
            return false;
        }
        Answer other = (Answer) obj;
        return correct == other.correct && Arrays.equals(recipe, other.recipe);
    }
    
    public boolean isCorrect()
    {
        return correct;
    }
}
