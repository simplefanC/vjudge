package judge.remote.provider.csu;

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
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

@Component
public class CSUSubmitter extends ComplexSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return CSUInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
        return info.remoteRunId != null ? Integer.parseInt(info.remoteRunId) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        HttpEntity entity = SimpleNameValueEntityFactory.create(
            "language", info.remotelanguage, //
            "pid", info.remoteProblemId, //
            "source", info.sourceCode
        );
        HttpPost post = new HttpPost("/csuoj/Problemset/submit_ajax");
        post.setEntity(entity);
        post.setHeader("X-Requested-With", "XMLHttpRequest");
        String html =  client.execute(post,
                new HttpBodyValidator("\"msg\":\"Submit successful")).getBody();
        Matcher matcher = Pattern.compile("\"solution_id\":\"(\\d+)\"").matcher(html);
        if(matcher.find()){
            info.remoteRunId = matcher.group(1);
        }
        return null;
    }

}
