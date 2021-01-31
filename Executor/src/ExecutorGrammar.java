public class ExecutorGrammar extends BaseGrammar {
    private static final String[] sa = new String[1];
    static{
        sa[0] = "BUFF_SIZE";
    }
    public ExecutorGrammar() {
        super(sa.length, sa);
    }
}
