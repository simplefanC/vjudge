package judge.remote.provider.hust;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.ComplexSubmitter;
import judge.remote.submitter.SubmissionInfo;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class HUSTSubmitter extends ComplexSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return HUSTInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
        String html = client.get("/status?uid=" + info.remoteAccountId + "&pid=" + info.remoteProblemId).getBody();
        Matcher matcher = Pattern.compile("/solution/source/(\\d+)").matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        HttpEntity entity = SimpleNameValueEntityFactory.create( //
            "language", info.remotelanguage, //
            "pid", info.remoteProblemId, //
            "source", info.sourceCode //
        );
        client.post("/problem/submit", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
        return null;
    }

}
