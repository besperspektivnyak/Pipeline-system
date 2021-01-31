public class ManagerGrammar extends BaseGrammar {
    private static final String[] params = new String[8];
    static{
        params[0] = "INPUT";
        params[1] = "INPUT_CFG";
        params[2] = "READER_NAME";
        params[3] = "OUTPUT";
        params[4] = "OUTPUT_CFG";
        params[5] = "WRITER_NAME";
        params[6] = "EXECUTOR_CFG";
        params[7] = "EXECUTOR_QUEUE";
    }
    public ManagerGrammar() {
        super(params.length, params);
    }
}