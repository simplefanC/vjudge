package judge.remote.crawler;

import judge.BaseJunitTest;
import judge.tool.Handler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * abstract test for direct crawl
 *
 * @author zzzz76
 */
public abstract class SimpleCrawlerTest extends BaseJunitTest {
    private final static Logger log = LoggerFactory.getLogger(SimpleCrawlerTest.class);

    @Test
    public abstract void testCrawl() throws Exception;

    protected Handler<RawProblemInfo> getHandler() {
        return new Handler<RawProblemInfo>() {
            @Override
            public void handle(RawProblemInfo info) {
                printProblem(info);
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
            }
        };
    }

    protected void terminal() {
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
