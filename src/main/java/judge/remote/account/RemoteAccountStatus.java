package judge.remote.account;

import java.util.HashSet;
import java.util.Stack;

import judge.remote.RemoteOj;

import org.apache.commons.lang3.Validate;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * 远程账户状态
 */
public class RemoteAccountStatus {

    private RemoteOj remoteOj;
    private String accountId;
    private String password;
    private boolean isPublic;
    private Stack<HttpContext> contexts;
    /**
     * 一个accountId不能拥有相同的Task
     */
    private HashSet<String> runningExclusiveCodes;
    private int contextsSize;

    /**
     * 可以选
     * @param accountId
     * @param exclusiveCode
     * @return
     */
    public boolean eligible(String accountId, String exclusiveCode) {
        return 
                contexts.size() > 0 && 
                (accountId == null || this.accountId.equals(accountId)) &&
                (exclusiveCode == null || !runningExclusiveCodes.contains(exclusiveCode));
    }
    
    public HttpContext borrowContext() {
//        log.info(contexts.size() + " > " + accountId);
        return contexts.pop();
    }
    
    public boolean returnContext(HttpContext context) {
//        log.info(contexts.size() + " < " + accountId);
        if (contexts.size() < contextsSize) {
            contexts.push(context);
            return true;
        }
        return false;
    }
    
    public void addExclusiveCode(String code) {
        if (code != null) {
            runningExclusiveCodes.add(code);
        }
    }
    
    public void removeExclusiveCode(String code) {
        if (code != null) {
            runningExclusiveCodes.remove(code);
        }
    }
    
    ////////////////////////////////////////////////////////////////

    /**
     * 构造函数
     * @param remoteOj
     * @param accountId
     * @param password
     * @param isPublic
     * @param contextsSize
     */
    public RemoteAccountStatus(
            RemoteOj remoteOj,
            String accountId,
            String password,
            boolean isPublic,
            int contextsSize) {
        super();
        Validate.isTrue(contextsSize > 0);
        this.remoteOj = remoteOj;
        this.accountId = accountId;
        this.password = password;
        this.isPublic = isPublic;
        this.contextsSize = contextsSize;
        this.contexts = new Stack<HttpContext>();

        while (contextsSize-- > 0) {
            contexts.push(getNewContext());
        }
        
        runningExclusiveCodes = new HashSet<String>();
    }

    public RemoteOj getRemoteOj() {
        return remoteOj;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPassword() {
        return password;
    }

    public boolean isPublic() {
        return isPublic;
    }

    private HttpContext getNewContext() {
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        return context;
    }

}
