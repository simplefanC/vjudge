package judge.remote.crawler;

import judge.BaseJunitTest;
import judge.tool.Handler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * abstract test for which need login to crawl
 *
 * @author zzzz76
 */
public abstract class AuthenticatedCrawlerTest extends BaseJunitTest {
    private final static Logger log = LoggerFactory.getLogger(AuthenticatedCrawlerTest.class);

    // 信号量
    private CountDownLatch doneSignal = new CountDownLatch(1);

    @Test
    public abstract void testCrawl() throws Exception;

    protected Handler<RawProblemInfo> getHandler() {
        return new Handler<RawProblemInfo>() {
            @Override
            public void handle(RawProblemInfo info) {
                printProblem(info);
                doneSignal.countDown();
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
                doneSignal.countDown();
            }
        };
    }

    protected void terminal() throws Exception {
        doneSignal.await(5, TimeUnit.MINUTES);
        System.err.println(">>> terminal");
    }

    private void printProblem(RawProblemInfo info) {
        System.err.println(info.title);
        System.err.println(String.valueOf(info.timeLimit));
        System.err.println(String.valueOf(info.memoryLimit));
        System.err.println(info.url);
        System.err.println(info.description);
        System.err.println(info.input);
        System.err.println(info.output);
        System.err.println(info.sampleInput);
        System.err.println(info.sampleOutput);
        System.err.println(info.hint);
        System.err.println(info.source);
        System.err.println(String.valueOf(new Date()));
    }
}
