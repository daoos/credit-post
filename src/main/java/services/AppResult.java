package services;

import java.util.List;

/**
 * Created by hadoop on 17-11-27.
 */
public class AppResult {
    public List<String> getParseResult() {
        return parseResult;
    }

    private final List<String> parseResult;
    public AppResult(List<String> parseResult){
        this.parseResult = parseResult;
    }

}
