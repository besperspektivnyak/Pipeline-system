import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IWriter;
import ru.spbstu.pipeline.RC;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Writer implements IWriter {

    private Logger LOGGER;
    private ConfigWriter cfg;

    private FileOutputStream outputStr;
    private int buffSize;
    public IExecutable producer;
    public IExecutable consumer;

    public Writer(Logger logger) { LOGGER = logger; cfg = new ConfigWriter(logger); }


    public RC WriteFile(byte[] buff) {
        try {
            outputStr.write(buff, 0, buff.length);
        }
        catch(IOException ex) {
            LOGGER.log(Level.SEVERE, "Can not write");
            return RC.CODE_FAILED_TO_WRITE;
        }
        LOGGER.log(Level.INFO, "Successfully write in file");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setOutputStream(FileOutputStream fos) {
        if(fos != null){
            outputStr = fos;
            LOGGER.log(Level.INFO, "Writer's output stream is set");
        }
        else{
            LOGGER.log(Level.SEVERE, "Invalid output stream");
            return RC.CODE_INVALID_OUTPUT_STREAM;
        }
        return RC.CODE_SUCCESS;
    }

    private RC SemanticParser(String cfgName){
        WriterGrammar gr = new WriterGrammar();
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
        LOGGER.log(Level.INFO,"Parse successful");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String cfg) {
        return SemanticParser(cfg);
    }

    @Override
    public RC setConsumer(IExecutable c) {
        consumer = null;
        LOGGER.log(Level.INFO, "Writer's consumer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IExecutable p) {
        producer = p;
        LOGGER.log(Level.INFO, "Writer's producer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] data) {
        RC codeErr = RC.CODE_SUCCESS;
        if (data != null){
            codeErr = WriteFile(data);
            if (codeErr != RC.CODE_SUCCESS) {
                LOGGER.log(Level.SEVERE, "Failed to write data");
                codeErr = RC.CODE_FAILED_TO_WRITE;
            }
        }
        else {
            codeErr = RC.CODE_FAILED_TO_WRITE;
        }
        return codeErr;
    }
}
