package judge.remote.provider.poj;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.RetentiveLoginer;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class POJLoginer extends RetentiveLoginer {

    @Override
    public RemoteOjInfo getOjInfo() {
        return POJInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/").getBody().contains(">Log Out</a>")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "B1", "login", //
                "password1", account.getPassword(), //
                "url", "%2F", //
                "user_id1", account.getAccountId());
        client.post("/login", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

}
