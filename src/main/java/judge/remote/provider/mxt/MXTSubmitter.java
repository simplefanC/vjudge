package judge.remote.provider.mxt;

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

import java.util.Objects;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-11-22
 */
@Component
public class MXTSubmitter extends SimpleSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return MXTInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected String getMaxRunId(SubmissionInfo info, DedicatedHttpClient client) throws Exception {
        return info.remoteRunId;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws Exception {
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "language", info.remotelanguage,
                "notes_id", "0",
                "source", info.sourceCode
        );
        HttpPost post = new HttpPost("/submit/7/"+ info.remoteProblemId +"/");
        post.setEntity(entity);
        String result = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        GsonUtil gsonUtil = new GsonUtil(result);
        String status = gsonUtil.getStrMem("success");
        if (Objects.equals(status, "false")) {
            // 返回错误信息
            return gsonUtil.getStrMem("msg");
        } else {
            info.remoteRunId = gsonUtil.getStrMem("solution_id");
            return null;
        }
    }

}
