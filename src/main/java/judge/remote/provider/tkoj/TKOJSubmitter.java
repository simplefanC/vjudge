package judge.remote.provider.tkoj;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.ComplexSubmitter;
import judge.remote.submitter.SubmissionInfo;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-12-10
 */
@Component
public class TKOJSubmitter extends ComplexSubmitter {
    @Override
    public RemoteOjInfo getOjInfo() {
        return TKOJInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected long getSubmitReceiptDelay() {
        return 10000;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) throws Exception {
        // 从状态页面中中获取max runid
        String html = client.get("/status.php?problem_id=" + info.remoteProblemId + "&user_id=" + info.remoteAccountId, HttpStatusValidator.SC_OK).getBody();
        Matcher matcher = Pattern.compile("prevtop=(\\d+)").matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws Exception {
        // 进行代码提交
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "id", info.remoteProblemId,
                "language", info.remotelanguage,
                "source", info.sourceCode,
                "csrf", TKOJVerifyUtil.getCsrf(client),
                "vcode", TKOJVerifyUtil.getCaptcha(client)
        );
        HttpPost post = new HttpPost("/submit.php");
        post.setEntity(entity);
        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
        return null;
    }
}
