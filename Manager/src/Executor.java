
import ru.spbstu.pipeline.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Executor implements IExecutor {

    private Logger LOGGER;
    private ConfigExecutor cfg;

    private int buffSize;
    private byte[] data;
    private TYPE[] outputTypes;
    private IMediator mediator;

    public IProducer producer;
    public IConsumer consumer;


    public Executor(Logger logger) {
        LOGGER = logger;
        cfg = new ConfigExecutor(logger);
        outputTypes = new TYPE[] {TYPE.BYTE};
    }

    public RC SemanticParser(String cfgName){
        ExecutorGrammar gr = new ExecutorGrammar();
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
        LOGGER.log(Level.INFO, "Successful parse");
        return RC.CODE_SUCCESS;
    }

    private RC BitInverting(byte[] buff)
    {
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte)(~((byte[])buff)[i]);
        }
        LOGGER.log(Level.INFO, "Bit Inverting executed successfully");
        return RC.CODE_SUCCESS;
    }


    @Override
    public RC setConfig(String cfg) {
        return SemanticParser(cfg);
    }

    @Override
    public RC setConsumer(IConsumer c) {
        consumer = c;
        LOGGER.log(Level.INFO, "Executor's consumer is set");
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IProducer p) {
        producer = p;
        TYPE dataType = typeIntersection();
        if(dataType != null){
            mediator = producer.getMediator(dataType);
        }
        else
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        LOGGER.log(Level.INFO, "Executor's producer is set");
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
        data = (byte[])mediator.getData();
        RC codeErr;
        if (data != null){
            codeErr = BitInverting(data);
            if (codeErr == RC.CODE_SUCCESS) {
                codeErr = consumer.execute();
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

    private class ByteMediator implements IMediator{

        @Override
        public Object getData() {
            if(data != null){
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
                    LOGGER.log(Level.SEVERE, "Can't converte data from byte to char");
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
    public TYPE[] getOutputTypes() {
        return outputTypes;
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
