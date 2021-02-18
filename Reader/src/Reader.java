import ru.spbstu.pipeline.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reader implements IReader {
    private Logger LOGGER;
    private ConfigReader cfg;
    private RC errCode;

    private FileInputStream inputStr;
    private int buffSize;
    private TYPE[] outputTypes;
    private byte[] data;

    public IProducer producer;
    public IConsumer consumer;


    public Reader(Logger logger) {
        LOGGER = logger;
        cfg = new ConfigReader(logger);
        outputTypes = new TYPE[] {TYPE.CHAR, TYPE.SHORT, TYPE.BYTE};
    }

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
        String[] valueNames = new String[gr.NumberGrammarTokens()];

        if(cfg.Parse(cfgName, resParams, valueNames) == RC.CODE_SUCCESS){
            for(int i = 0; i < resParams.length; i++) {
                if (gr.GrammarToken(i).equals(valueNames[0])) {
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
    public RC setConsumer(IConsumer c) {
        consumer = c;
        LOGGER.log(Level.INFO, "Reader's consumer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IProducer p) {
        producer = null;
        LOGGER.log(Level.INFO, "Reader's producer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute() {
        data =  new byte[buffSize];
        while(data != null) {
            data = ReadFile();
            if (errCode == RC.CODE_SUCCESS) {
                errCode = consumer.execute();
            }
            else {
                errCode = RC.CODE_FAILED_TO_READ;
                LOGGER.log(Level.SEVERE, "Failed to read");
            }
        }
        return errCode;
    }

    @Override
    public TYPE[] getOutputTypes() {
        return outputTypes;
    }

    private class ByteMediator implements IMediator{

        @Override
        public Object getData() {
            if (data != null){
                byte[] newData = new byte[buffSize];
                System.arraycopy(data, 0, newData, 0, buffSize);
                return newData;
            }
            else
                return null;

        }
    }

    private class CharMediator implements IMediator{

        @Override
        public Object getData() {
            if (data != null) {
                try {
                    return new String(data, "UTF-8").toCharArray();
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE, "Can't convert data from byte to char");
                    return null;
                }
            }
            else {
                return null;
            }
        }
    }

    private class ShortMediator implements IMediator{

        @Override
        public Object getData() {
            short[] newData = new short[data.length / 2];
            if (data != null) {
                ByteBuffer bf = ByteBuffer.wrap(data);
                for(int i = 0; i < data.length / 2; i++){
                    newData[i] = bf.getShort(i * 2);
                }
                return newData;
            }
            return null;
        }
    }

    @Override
    public IMediator getMediator(TYPE type) {
        switch (type){
            case BYTE:
                return new ByteMediator();
            case CHAR:
                return new CharMediator();
            case SHORT:
                return new ShortMediator();
            default:
                LOGGER.log(Level.SEVERE, "incompatible type" + type);
                return null;
        }
    }
}
