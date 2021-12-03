package judge.remote.provider.tkoj;

import judge.remote.RemoteOjInfo;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-12-10
 */
@Component
public class TKOJLanguageFinder implements LanguageFinder {
    @Override
    public RemoteOjInfo getOjInfo() {
        return TKOJInfo.INFO;
    }

    @Override
    public boolean isDiverse() {
        return false;
    }

    @Override
    public void getLanguages(String remoteProblemId, Handler<LinkedHashMap<String, String>> handler) {

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
        languageList.put("7", "PHP");
        languageList.put("8", "Perl");
        languageList.put("9", "C#");
        languageList.put("10", "Obj-C");
        languageList.put("11", "FreeBasic");
        languageList.put("12", "Scheme");
        languageList.put("13", "Clang");
        languageList.put("14", "Clang++");
        languageList.put("15", "Lua");
        languageList.put("16", "JavaScript");
        languageList.put("17", "Go");
        languageList.put("18", "SQL(sqlite3)");
        return languageList;
    }

    @Override
    public HashMap<String, String> getLanguagesAdapter() {
        return null;
    }
}
