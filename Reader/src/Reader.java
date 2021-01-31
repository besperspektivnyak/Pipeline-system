import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.RC;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reader implements IReader {
    private Logger LOGGER;
    private ConfigReader cfg;
    private RC errCode;

    private FileInputStream inputStr;
    private int buffSize;
    public IExecutable producer;
    public IExecutable consumer;

    public Reader(Logger logger) { LOGGER = logger; cfg = new ConfigReader(logger); }

    private byte[] ReadFile(){
        int num_bytes = 0;
        byte[] buff = new byte[buffSize];
        try{
            num_bytes = inputStr.read(buff);
            if (num_bytes < buffSize) {
                RemoveZeros(buff);
            }
        }
        catch(IOException ex){
            errCode = RC.CODE_FAILED_TO_READ;
            LOGGER.log(Level.SEVERE, "Failed to read");
            return null;
        }
        if(num_bytes == -1){
            return null;
        }
        errCode = RC.CODE_SUCCESS;
        LOGGER.log(Level.INFO, "Read successful");
        return buff;
    }

    private void RemoveZeros(byte[] buff){
        for(int i = 0; i < buff.length; i++){
            if(buff[i] != 0){
                buff[i] = buff[i];
            }
        }
    }

    private RC SemanticParser(String cfgName){
        ReaderGrammar gr = new ReaderGrammar();
        String[] resParams = new String[gr.NumberGrammarTokens()];
        if(cfg.Parse(cfgName, resParams) == RC.CODE_SUCCESS){
            for(int i = 0; i < resParams.length; i++) {
                if (gr.GrammarToken(i).equals("BUFF_SIZE")) {
                    buffSize = Integer.parseInt(resParams[i]);
                }
                else{
                    LOGGER.log(Level.SEVERE, "Semantic error in config file");
                    return RC.CODE_CONFIG_SEMANTIC_ERROR;
                }
            }
        }
        else{
            LOGGER.log(Level.SEVERE, "Grammar error in config file");
            return RC.CODE_CONFIG_GRAMMAR_ERROR;
        }
        LOGGER.log(Level.INFO, "Successful parse");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setInputStream(FileInputStream fis) {
        if(fis != null){
            inputStr = fis;
            LOGGER.log(Level.INFO, "Reader's input stream is set");
            return RC.CODE_SUCCESS;
        }
        LOGGER.log(Level.SEVERE, "Invalid input");
        return RC.CODE_INVALID_INPUT_STREAM;
    }

    @Override
    public RC setConfig(String cfg) {
        return SemanticParser(cfg);
    }

    @Override
    public RC setConsumer(IExecutable c) {
        consumer = c;
        LOGGER.log(Level.INFO, "Reader's consumer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IExecutable p) {
        producer = null;
        LOGGER.log(Level.INFO, "Reader's producer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] data) {
        data = ReadFile();
        if (errCode == RC.CODE_SUCCESS) {
            errCode = consumer.execute(data);
        }
        else {
            errCode = RC.CODE_FAILED_TO_READ;
            LOGGER.log(Level.SEVERE, "Failed to read");
        }
        return errCode;
    }
}
