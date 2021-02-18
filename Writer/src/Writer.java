import ru.spbstu.pipeline.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Writer implements IWriter {

    private Logger LOGGER;
    private ConfigWriter cfg;

    private FileOutputStream outputStr;
    private int buffSize;
    private TYPE[] outputTypes;
    private TYPE dataType;
    private IMediator mediator;
    private Object data;

    public IProducer producer;
    public IConsumer consumer;

    public Writer(Logger logger) {
        LOGGER = logger;
        cfg = new ConfigWriter(logger);
        outputTypes = new TYPE[] {TYPE.CHAR, TYPE.SHORT, TYPE.BYTE};
    }


    public RC WriteFile(byte[] buff) {
        try {
            int zeros = CheckZeros(buff);
            if(zeros != 0){
                byte[] newBuff = RemoveZeros(buff, zeros);
                outputStr.write(buff, 0, newBuff.length);
            }
            else
                outputStr.write(buff, 0, buff.length);
        }
        catch(IOException ex) {
            LOGGER.log(Level.SEVERE, "Can not write");
            return RC.CODE_FAILED_TO_WRITE;
        }
        LOGGER.log(Level.INFO, "Successfully write in file");
        return RC.CODE_SUCCESS;
    }

    private int CheckZeros(byte[] buff){
        int i;
        for(i = 0; i < buff.length; i++){
            if(buff[i] == 0){
                break;
            }
        }
        return i;
    }

    private byte[] RemoveZeros(byte[] buff, int newSize){
        byte[] newBuff = new byte[newSize];
        System.arraycopy(buff, 0, newBuff, 0, newSize);
        return newBuff;
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
        String[] valueNames = new String[gr.NumberGrammarTokens()];

        if(cfg.Parse(cfgName, resParams, valueNames) == RC.CODE_SUCCESS){
            for(int i = 0; i < resParams.length; i++) {
                if (gr.GrammarToken(i).equals(valueNames[0])) {
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
    public RC setConsumer(IConsumer c) {
        consumer = null;
        LOGGER.log(Level.INFO, "Writer's consumer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IProducer p) {
        producer = p;
        dataType = typeIntersection();
        if(dataType != null){
            mediator = producer.getMediator(dataType);
        }
        else
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        LOGGER.log(Level.INFO, "Writer's producer is set");
        return RC.CODE_SUCCESS;
    }

    private TYPE typeIntersection(){
        TYPE[] producerTypes = producer.getOutputTypes();
        for(TYPE type: outputTypes){
            for (int i = 0, producerTypesLength = producerTypes.length; i < producerTypesLength; i++) {
                TYPE producerType = producerTypes[i];
                if (producerType == type) {
                    mediator = producer.getMediator(producerType);
                    LOGGER.log(Level.INFO, "Find type in executor" + producerType);
                    return producerType;
                }
            }
        }
        LOGGER.log(Level.SEVERE, "There is not any compatible type");
        return null;
    }


    @Override
    public RC execute() {
        RC codeErr;
        data = mediator.getData();
        byte[] newData = convertToByte(dataType);
        if (newData != null){
            codeErr = WriteFile(newData);
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

    private byte[] convertFromShort(short[] data){
        byte[] newData = new byte[data.length * 2];
        ByteBuffer.wrap(newData).asShortBuffer().put(data);
        return newData;
    }

    private byte[] convertFromChar(char[] data) {
        try {
            return new String(data).getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException ex){
            LOGGER.log(Level.SEVERE, "Can't convert data from char to byte");
            return null;
        }
    }

    private byte[] convertFromByte(byte[] data){
        return data;
    }

    private byte[] convertToByte(TYPE type){
        switch (type){
            case SHORT:
                return convertFromShort((short[])data);
            case BYTE:
                return convertFromByte((byte[])data);
            case CHAR:
                return convertFromChar((char[])data);
            default:
                LOGGER.log(Level.SEVERE, "Incompatible types");
                return null;
        }
    }
}
