package judge.remote.loginer;

import judge.BaseJunitTest;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

/**
 * abstract test for login
 *
 * @author zzzz76
 */
public abstract class LoginerTest extends BaseJunitTest {

    protected HttpContext getNewContext() {
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        return context;
    }

    protected void terminal() {
        System.err.println(">>> terminal");
    }

    @Test
    public abstract void testLogin() throws Exception;
}
