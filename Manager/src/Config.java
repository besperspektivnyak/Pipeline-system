import ru.spbstu.pipeline.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    private Logger LOGGER;

    protected String inputFile;
    protected String readerCfg;
    protected String readerName;

    protected String outputFile;
    protected String writerCfg;
    protected String writerName;

    protected String[] executorCfg;
    protected String[] queue;

    private final String executorsDelimeter = " ";

    public Config(Logger logger) { LOGGER = logger; }

    protected RC ParseConfig(String config_name, String[] resParams) {
        try {
            BufferedReader inputStr = new BufferedReader(new FileReader(config_name));
            String tmp;
            String[] paramTmp;
            ManagerGrammar grammar = new ManagerGrammar();

            while ((tmp = inputStr.readLine()) != null) {
                paramTmp = tmp.split(grammar.Delimeter());

                if (paramTmp[0].equals(grammar.GrammarToken(0))) {
                    resParams[0] = paramTmp[1];
                } else if (paramTmp[0].equals(grammar.GrammarToken(1))) {
                    resParams[1] = paramTmp[1];
                } else if (paramTmp[0].equals(grammar.GrammarToken(2))) {
                    resParams[2] = paramTmp[1];
                } else if (paramTmp[0].equals(grammar.GrammarToken(3))) {
                    resParams[3] = paramTmp[1];
                } else if (paramTmp[0].equals(grammar.GrammarToken(4))) {
                    resParams[4] = paramTmp[1];
                } else if (paramTmp[0].equals(grammar.GrammarToken(5))) {
                    resParams[5] = paramTmp[1];
                } else if (paramTmp[0].equals(grammar.GrammarToken(6))) {
                    resParams[6] = paramTmp[1];
                } else if (paramTmp[0].equals(grammar.GrammarToken(7))) {
                    resParams[7] = paramTmp[1];
                } else {
                    LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_CONFIG_GRAMMAR_ERROR));
                    return RC.CODE_CONFIG_GRAMMAR_ERROR;
                }
            }
        } catch (IOException ex){
            LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_INVALID_INPUT_STREAM));
            return RC.CODE_INVALID_INPUT_STREAM;

        }
        LOGGER.log(Level.INFO, "Manager parsing is successful");
        return RC.CODE_SUCCESS;
    }

    protected RC ConfigInit(String[] startParams){
        ManagerGrammar gr = new ManagerGrammar();

        if(startParams != null){
            for(int i = 0; i < startParams.length; i++) {
                if(gr.GrammarToken(i).equals("INPUT")){
                    inputFile = startParams[i];
                }
                else if(gr.GrammarToken(i).equals("INPUT_CFG")) {
                    readerCfg = startParams[i];
                }
                else if(gr.GrammarToken(i).equals("OUTPUT")) {
                    outputFile = startParams[i];
                }
                else if(gr.GrammarToken(i).equals("OUTPUT_CFG")) {
                    writerCfg = startParams[i];
                }
                else if(gr.GrammarToken(i).equals("EXECUTOR_CFG")) {
                    executorCfg = startParams[i].split(executorsDelimeter);
                }
                else if(gr.GrammarToken(i).equals("EXECUTOR_QUEUE")) {
                    queue = startParams[i].split(executorsDelimeter);
                }
                else if(gr.GrammarToken(i).equals("READER_NAME")) {
                    readerName = startParams[i];
                }
                else if(gr.GrammarToken(i).equals("WRITER_NAME")) {
                    writerName = startParams[i];
                }
                else {
                    LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_CONFIG_GRAMMAR_ERROR));
                    return RC.CODE_CONFIG_GRAMMAR_ERROR;
                }

            }
        }
        else {
            LOGGER.log(Level.SEVERE, "Invalid argument");
            return RC.CODE_INVALID_ARGUMENT;
        }
        LOGGER.log(Level.INFO, "Semantic parse is successful");
        return RC.CODE_SUCCESS;
    }
}
