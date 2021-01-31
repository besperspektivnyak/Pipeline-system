
import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IExecutor;
import ru.spbstu.pipeline.RC;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Executor implements IExecutor {

    private Logger LOGGER;
    private ConfigExecutor cfg;

    private int buffSize;
    public IExecutable producer;
    public IExecutable consumer;
    private String str;

    public Executor(Logger logger) { LOGGER = logger; cfg = new ConfigExecutor(logger); }


    public RC SemanticParser(String cfgName){
        ExecutorGrammar gr = new ExecutorGrammar();
        String[] resParams = new String[gr.NumberGrammarTokens()];
        if(cfg.Parse(cfgName, resParams) == RC.CODE_SUCCESS){
            for(int i = 0; i < resParams.length; i++) {
                if (gr.GrammarToken(i).equals("BUFF_SIZE")) {
                    buffSize = Integer.parseInt(resParams[i]);
                }
                else{
                    LOGGER.log(Level.SEVERE, "Semantic error in config");
                    return RC.CODE_CONFIG_SEMANTIC_ERROR;
                }

            }
        }
        else{
            LOGGER.log(Level.SEVERE, "Grammar error in config");
            return RC.CODE_CONFIG_GRAMMAR_ERROR;
        }
        LOGGER.log(Level.INFO, "Successful parse");
        return RC.CODE_SUCCESS;
    }

    private RC BitInverting(byte[] buff)
    {
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) (~buff[i]);
        }
        LOGGER.log(Level.INFO, "Bit Inverting executed successfully");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String cfg) {
        return SemanticParser(cfg);
    }

    @Override
    public RC setConsumer(IExecutable c) {
        consumer = c;
        LOGGER.log(Level.INFO, "Executor's consumer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IExecutable p) {
        producer = p;
        LOGGER.log(Level.INFO, "Executor's producer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] data) {
        RC codeErr = null;
        if (data != null){
            codeErr = BitInverting(data);
            if (codeErr == RC.CODE_SUCCESS) {
                codeErr = consumer.execute(data);
            }
            else {
                codeErr = RC.CODE_FAILED_TO_READ;
                LOGGER.log(Level.SEVERE, "Failed to invert bytes");
            }
        }
        else{
            codeErr = RC.CODE_INVALID_ARGUMENT;
            LOGGER.log(Level.SEVERE, "Invalid argument");
        }
        return codeErr;
    }
}
