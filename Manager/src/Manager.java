import ru.spbstu.pipeline.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager extends ManagerGrammar {

    private Logger LOGGER;
    private Config cfg;
    public byte[] buffer;

    private FileInputStream inputStream;
    private FileOutputStream outputStream;

    private IReader reader;
    private IWriter writer;
    private IExecutable executors[];
    private IPipelineStep[] pipeline;

    public Manager(Logger logger){
        LOGGER = logger;
        cfg = new Config(logger);
    }

    public RC ManagerStart(String[] startParams) {
        if (cfg.ConfigInit(startParams) == RC.CODE_SUCCESS) {
            IExecutor[] queue = new IExecutor[cfg.queue.length];
            if (InitExecutors(cfg.queue) == RC.CODE_SUCCESS) {
                InitReader(cfg.readerName);
                InitWriter(cfg.writerName);
                pipeline = InitQueue();
                LOGGER.log(Level.INFO, "Manager initialized all executors");
                return RC.CODE_SUCCESS;
            } else {
                LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_CONFIG_SEMANTIC_ERROR));
                return RC.CODE_CONFIG_SEMANTIC_ERROR;
            }
        } else {
            LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_CONFIG_SEMANTIC_ERROR));
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
    }


    private void CreateReader(String className){
        try{
            Class obj = Class.forName(className);
            reader = (IReader)obj.getConstructor(Logger.class).newInstance(LOGGER);
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex){
            LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_CONFIG_SEMANTIC_ERROR));

        }
        LOGGER.log(Level.INFO, "Reader was created");
    }

    private void CreateWriter(String className){
        try{
            Class obj = Class.forName(className);
            writer = (IWriter)obj.getConstructor(Logger.class).newInstance(LOGGER);
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex){
            LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_CONFIG_SEMANTIC_ERROR));

        }
        LOGGER.log(Level.INFO, "Writer was created");
    }

    private void InitReader(String className){
        CreateReader(className);
        reader.setConfig(cfg.readerCfg);
        if (OpenFis() == RC.CODE_SUCCESS)
            reader.setInputStream(inputStream);
    }

    private void InitWriter(String className){
        CreateWriter(className);
        writer.setConfig(cfg.writerCfg);
        if (OpenFos() == RC.CODE_SUCCESS)
            writer.setOutputStream(outputStream);
    }

    private IExecutable CreateExecutor(String className){
        IExecutable executor;
        try{
            Class obj = Class.forName(className);
            executor = (IExecutable)obj.getConstructor(Logger.class).newInstance(LOGGER);
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex){
            LOGGER.log(Level.SEVERE, RCMeaning.getMeaning(RC.CODE_CONFIG_SEMANTIC_ERROR));
            return null;
        }
        LOGGER.log(Level.INFO, RCMeaning.getMeaning(RC.CODE_SUCCESS));
        return executor;
    }



    private RC InitExecutors(String[] queue) {
        executors = new IExecutable[queue.length];
        for (int i = 0; i < queue.length; i++) {
            IExecutable newExec = CreateExecutor(queue[i]);
            if (newExec != null) {
                executors[i] = newExec;
                ((IConfigurable)executors[i]).setConfig(cfg.executorCfg[i]);
            }
            else {
                LOGGER.log(Level.SEVERE, "Failed to build " + queue[i]);
                return RC.CODE_CONFIG_SEMANTIC_ERROR;
            }
        }
        LOGGER.log(Level.INFO, "Executors were initialized");
        return RC.CODE_SUCCESS;
    }

    private IPipelineStep[] InitQueue() {
        pipeline = new IPipelineStep[2 + executors.length];
        pipeline[0] = reader;
        for (int i = 1; i < pipeline.length - 1; i++) {
            pipeline[i] = (IPipelineStep)executors[i-1];
            pipeline[i-1].setConsumer(pipeline[i]);
            pipeline[i].setProducer(pipeline[i-1]);
        }
        pipeline[pipeline.length - 1] = writer;
        pipeline[pipeline.length - 2].setConsumer(pipeline[pipeline.length - 1]);
        pipeline[pipeline.length - 1].setProducer(pipeline[pipeline.length - 2]);
        return pipeline;
    }

    private RC CloseStreams() {
        try {
            inputStream.close();
        }
        catch (IOException ex){
            LOGGER.log(Level.SEVERE, "Can not close input stream.");
            return RC.CODE_INVALID_INPUT_STREAM;
        }
        try{
            outputStream.close();
        }
        catch (IOException ex){
            LOGGER.log(Level.SEVERE, "Can not close output stream.");
            return RC.CODE_INVALID_OUTPUT_STREAM;
        }
        return RC.CODE_SUCCESS;
    }

    public RC Run(){
        reader.execute(buffer);
        RC code = CloseStreams();
        if (code != RC.CODE_SUCCESS) {
            return code;
        }
        else{
            return RC.CODE_SUCCESS;
        }
    }

    private RC OpenFis(){
        try{
            inputStream = new FileInputStream(cfg.inputFile);
            return RC.CODE_SUCCESS;
        }
        catch(IOException ex){
            LOGGER.log(Level.INFO, "Can not open input stream");
            return RC.CODE_INVALID_INPUT_STREAM;
        }
    }

    private RC OpenFos(){
        try{
            outputStream = new FileOutputStream(cfg.outputFile);
            return RC.CODE_SUCCESS;
        }
        catch(IOException ex){
            LOGGER.log(Level.INFO, "Can not open output stream");
            return RC.CODE_INVALID_OUTPUT_STREAM;
        }
    }
}