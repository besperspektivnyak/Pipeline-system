import ru.spbstu.pipeline.RC;

public class RCMeaning {
    public static String getMeaning(RC code){
        switch(code){
            case CODE_SUCCESS:
                return "Success!";
            case CODE_CONFIG_SEMANTIC_ERROR:
                return "Semantic error in config file.";
            case CODE_INVALID_ARGUMENT:
                return "Error with argument. Check all arguments!";
            case CODE_INVALID_INPUT_STREAM:
                return "Problems with input stream.";
            case CODE_INVALID_OUTPUT_STREAM:
                return "Problems with output stream.";
            case CODE_CONFIG_GRAMMAR_ERROR:
                return "Grammar error in config file. Check names!";
            case CODE_FAILED_TO_READ:
                return "Reading was not successful.";
            case CODE_FAILED_TO_WRITE:
                return "Writing was not successful.";
            case CODE_FAILED_PIPELINE_CONSTRUCTION:
                return "Pipeline was not construct :(";
            default:
                return null;
        }
    }
}
