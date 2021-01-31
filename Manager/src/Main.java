import ru.spbstu.pipeline.RC;

import java.io.IOException;
import java.util.logging.*;


public class Main {
    public static final Logger LOGGER = Logger.getLogger(Logger.class.getName());

    private static RC errCode = RC.CODE_SUCCESS;

    public static void main(String[] args) {
        Config cfg = new Config(LOGGER);

        try{
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("logging.properties"));
        } catch (NullPointerException | IOException ex) {
            errCode = RC.CODE_INVALID_ARGUMENT;
        }
        ManagerGrammar gr = new ManagerGrammar();
        String[] resParams = new String[gr.NumberGrammarTokens()];
        if (args != null){
            errCode = cfg.ParseConfig(args[0], resParams);
            if (errCode == RC.CODE_SUCCESS){
                Manager mg = new Manager(LOGGER);
                errCode = mg.ManagerStart(resParams);
                if (errCode == RC.CODE_SUCCESS) {
                    errCode = mg.Run();
                }
            }
            System.out.print(RCMeaning.getMeaning(errCode));
        }
    }
}

