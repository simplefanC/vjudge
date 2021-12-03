package judge.remote.provider.jsk;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.SubmissionInfo;
import judge.tool.GsonUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.hibernate.type.ObjectType;
import org.hibernate.type.PostgresUUIDType;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-10-13
 */
@Component
public class JSKQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
    }

    // 统计计蒜客的所有静态原生状态 --> unkown error 暂时未统计
    private static final Map<String, String> statusMap = new HashMap<String, String>() {{
        put("AC", "Accepted");
        put("PE", "Presentation Error");
        put("WA", "Wrong Answer");
        put("TL", "Time Limit Exceeded");
        put("ML", "Memory Limit Exceeded");
        put("OL", "Output Limit Exceeded");
        put("RE", "Runtime Error");
        put("SF", "Segmentation Fault");
        put("AE", "Arithmetical Error");
        put("CE", "Compilation Error");
        put("pending", "Queuing");
        put("fail", "Submit Failed");
    }};

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws Exception {

        String token = JSKTokenUtil.getToken(client);
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "id", info.remoteProblemId
        );
        HttpPost post = new HttpPost("/solve/check/" + info.remoteRunId);
        post.setEntity(entity);
        post.setHeader("X-Requested-With", "XMLHttpRequest");
        post.setHeader("X-XSRF-TOKEN", token);
        String post_json = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        GsonUtil gsonUtil = new GsonUtil(post_json);
        String status_code = gsonUtil.getStrMem("status");
        if ("finished".equals(status_code)) {
            status_code = gsonUtil.getStrMem("reason", "data");
        }

        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = statusMap.getOrDefault(status_code, status_code);
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);

        if (status.statusType == RemoteStatusType.AC) {
            // 补充执行时间和内存
            HttpGet get = new HttpGet("/t/" + info.remoteProblemId + "/submissions");
            get.setHeader("X-Requested-With", "XMLHttpRequest");
            get.setHeader("X-XSRF-TOKEN", token);
            String get_json = client.execute(get, HttpStatusValidator.SC_OK).getBody();
            List<Map<String, Object>> list = GsonUtil.jsonToListMaps(new GsonUtil(get_json).getStrMem("data"));
            status.executionTime = ((Double) list.get(0).get("used_time")).intValue();
            status.executionMemory = ((Double) list.get(0).get("used_mem")).intValue();

        } else if (status.statusType == RemoteStatusType.CE) {
            // 补充编译错误信息
            status.compilationErrorInfo = gsonUtil.getStrMem("message", "data");
        }
        return status;
    }

}
