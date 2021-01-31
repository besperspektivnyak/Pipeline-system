public abstract class BaseGrammar {
    private final int numberGrammarTokens;
    private static final String delimeter = "=";
    private final String[] tokens;
    protected BaseGrammar(int numTokens, String[] newTokens){
        numberGrammarTokens = numTokens;
        tokens = new String[numberGrammarTokens];
        for (int i = 0; i < numberGrammarTokens; i++){
            tokens[i] = newTokens[i];
        }
    }
    public int NumberGrammarTokens(){
        return numberGrammarTokens;
    }
    public String Delimeter(){
        return delimeter;
    }
    public String GrammarToken(int index){
        if(index > -1 && index < numberGrammarTokens){
            return tokens[index];
        }
        return null;
    }
}

