package judge.remote.provider.jsk;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.RetentiveLoginer;
import judge.tool.MD5;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-10-09
 */
@Component
public class JSKLoginer extends RetentiveLoginer {

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.LOGIN;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) throws Exception {
        if (client.get("/?n=https://nanti.jisuanke.com/oi#/").getBody().contains("i.jisuanke.com/setting/basic")) {
            return;
        }

        String token = JSKTokenUtil.getToken(client);
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "account", account.getAccountId(),
                "pwd", MD5.getMD5(account.getPassword()),
                "save", "1");

        // 在post请求中携带token绕过csrf验证
        HttpPost post = new HttpPost("/auth/login");
        post.setEntity(entity);
        post.setHeader("X-Requested-With", "XMLHttpRequest");
        post.setHeader("X-XSRF-TOKEN", token);
        client.execute(post, HttpStatusValidator.SC_OK, new HttpBodyValidator("success"));
    }

}
