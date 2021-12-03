package judge.remote.provider.aizu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.ComplexSubmitter;
import judge.remote.submitter.SubmissionInfo;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class AizuSubmitter extends ComplexSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return AizuInfo.INFO;
    }


    @Override
    protected boolean needLogin() {
        return false;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
        String html = client.get("/onlinejudge/status.jsp").getBody();
        Matcher matcher = Pattern.compile("id=\"run_(\\d+)(?:[\\s\\S](?!\\/tr))*>" + info.remoteAccountId + "<(?:[\\s\\S](?!\\/tr))*description\\.jsp\\?id=" + info.remoteProblemId).matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        HttpEntity entity = SimpleNameValueEntityFactory.create(
            "language", info.remotelanguage, //
            "password", remoteAccount.getPassword(), //
            "problemNO", info.remoteProblemId, //
            "sourceCode", info.sourceCode, //
            "userID", remoteAccount.getAccountId() //
        );
        client.post("/onlinejudge/servlet/Submit", entity, new HttpBodyValidator("HTTP-EQUIV=\"refresh\""));
        return null;
    }

}
