package judge.remote.provider.hysbz;

import java.util.HashMap;
import java.util.LinkedHashMap;

import judge.remote.RemoteOjInfo;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;

import org.springframework.stereotype.Component;

@Component
public class HYSBZLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOjInfo getOjInfo() {
        return HYSBZInfo.INFO;
    }

    @Override
    public boolean isDiverse() {
        return false;
    }

    @Override
    public void getLanguages(String remoteProblemId, Handler<LinkedHashMap<String, String>> handler) {
        // TODO Auto-generated method stub
    }

    @Override
    public LinkedHashMap<String, String> getDefaultLanguages() {
        LinkedHashMap<String, String> languageList = new LinkedHashMap<String, String>();
        languageList.put("0", "C");
        languageList.put("1", "C++");
        languageList.put("2", "Pascal");
        languageList.put("3", "Java");
        languageList.put("4", "Ruby");
        languageList.put("5", "Bash");
        languageList.put("6", "Python");
        return languageList;
    }

    @Override
    public HashMap<String, String> getLanguagesAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

}
