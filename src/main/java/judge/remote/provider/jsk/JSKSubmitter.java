package judge.remote.provider.jsk;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SimpleSubmitter;
import judge.tool.GsonUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-10-13
 */
@Component
public class JSKSubmitter extends SimpleSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected String getMaxRunId(SubmissionInfo info, DedicatedHttpClient client) throws Exception {
        return info.remoteRunId;
    }

    private static final Map<String, String> fileMap = new HashMap<String, String>() {{
        put("FAILED","Failed");
        put("c", "main.c");
        put("c_noi", "main.c");
        put("c++", "main.cpp");
        put("c++14", "main.cpp");
        put("c++_noi", "main.cpp");
        put("java", "Main.java");
        put("python", "main.py");
        put("python3", "main.py");
        put("ruby", "main.rb");
        put("blockly", "main.bl");
        put("octave", "main.m");
        put("pascal", "main.pas");
        put("go", "main.go");
    }};

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws Exception {
        String token = JSKTokenUtil.getToken(client);
        HttpEntity entity = SimpleNameValueEntityFactory.create(
            "codes[]", info.sourceCode,
            "file[]", fileMap.get(info.remotelanguage),
            "id", info.remoteProblemId,
            "language", info.remotelanguage
        );
        HttpPost post = new HttpPost("/solve/submit");
        post.setEntity(entity);
        post.setHeader("X-Requested-With", "XMLHttpRequest");
        post.setHeader("X-XSRF-TOKEN", token);
        String result = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        info.remoteRunId = new GsonUtil(result).getStrMem("data");
        return null;
    }

}
