package judge.remote.loginer;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.remote.account.RemoteAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 保持 登录
 */
public abstract class RetentiveLoginer implements Loginer {
    private final static Logger log = LoggerFactory.getLogger(RetentiveLoginer.class);
    /**
     * 记录上次登录时间戳
     * httpContext hashCode -> last login epoch millisecond
     */
    private final static ConcurrentHashMap<Integer, Long> lastLoginTimeMap = new ConcurrentHashMap<Integer, Long>();
    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public final void login(RemoteAccount account) throws Exception {
        int contextHashCode = account.getContext().hashCode();
        Long lastLoginTime = lastLoginTimeMap.get(contextHashCode);

        if (lastLoginTime == null || now() - lastLoginTime > getOjInfo().maxInactiveInterval) {
            log.info(String.format(
                    "Login: %s | %s | %s",
                    account.getRemoteOj(),
                    account.getAccountId(),
                    account.getExclusiveCode()));
            DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext(), getOjInfo().defaultChaset);
            loginEnforce(account, client);
        }
        lastLoginTimeMap.put(contextHashCode, now());
    }

    private long now() {
        return System.currentTimeMillis();
    }

    protected abstract void loginEnforce(RemoteAccount account, DedicatedHttpClient client) throws Exception;

}
