package judge.remote.provider.jsk;

import judge.remote.RemoteOjInfo;
import judge.remote.language.LanguageFinder;
import judge.tool.Handler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-10-09
 */
@Component
public class JSKLanguageFinder implements LanguageFinder {

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
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
        languageList.put("c", "C");
        languageList.put("c_noi", "C (NOI)");
        languageList.put("c++", "C++11");
        languageList.put("c++14", "C++14");
        languageList.put("c++_noi", "C++ (NOI)");
        languageList.put("java", "Java");
        languageList.put("python", "Python 2.7");
        languageList.put("python3", "Python 3.5");
        languageList.put("ruby", "Ruby");
        languageList.put("octave", "Octave");
        languageList.put("pascal", "Pascal");
        languageList.put("go", "Go");
        return languageList;
    }

    @Override
    public HashMap<String, String> getLanguagesAdapter() {
        // TODO Auto-generated method stub
        return null;
    }
}
