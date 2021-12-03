package judge.remote.provider.spoj;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.ComplexSubmitter;
import judge.remote.submitter.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class SPOJSubmitter extends ComplexSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return SPOJInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
        return submitted ? Integer.parseInt(info.remoteRunId) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        HttpEntity entity = SimpleNameValueEntityFactory.create(
            "lang", info.remotelanguage, //
            "problemcode", info.remoteProblemId, //
            "file", info.sourceCode, //
            getCharset() //
        );
        String html = client.post("/submit/complete/", entity).getBody();
        
        if (html.contains("submit in this language for this problem")) {
            return "Language Error";
        }
        if (html.contains("solution is too long")) {
            return "Code Length Exceeded";
        }
        
        info.remoteRunId = Tools.regFind(html, "name=\"newSubmissionId\" value=\"(\\d+)\"");
        Validate.isTrue(!StringUtils.isBlank(info.remoteRunId));
        return null;
    }

}
