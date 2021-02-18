import ru.spbstu.pipeline.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigExecutor {
    Logger LOGGER;

    public ConfigExecutor(Logger logger) { LOGGER = logger; }

    protected RC Parse(String config_name, String[] resParams, String[] valueNames) {
        try {
            BufferedReader inputStr = new BufferedReader(new FileReader(config_name));
            String tmp;
            String[] paramTmp;
            ExecutorGrammar grammar = new ExecutorGrammar();

            while ((tmp = inputStr.readLine()) != null) {
                paramTmp = tmp.split(grammar.Delimeter());
                if (paramTmp[0].equals(grammar.GrammarToken(0))) {
                    resParams[0] = paramTmp[1];
                    valueNames[0] = paramTmp[0];
                    LOGGER.log(Level.INFO, "Parse reader config was successful");
                }
                else {
                    LOGGER.log(Level.SEVERE, "Grammar error in config");
                    return RC.CODE_CONFIG_GRAMMAR_ERROR;
                }
            }
        }
        catch (IOException ex){
            LOGGER.log(Level.SEVERE, "Can not open or read reader's config");
            return RC.CODE_FAILED_TO_READ;

        }
        return RC.CODE_SUCCESS;
    }
}

