public class ReaderGrammar extends BaseGrammar {

    private static final String[] sa = new String[1];
    static{
        sa[0] = "BUFF_SIZE";
    }
    public ReaderGrammar() {
        super(sa.length, sa);
    }
}
