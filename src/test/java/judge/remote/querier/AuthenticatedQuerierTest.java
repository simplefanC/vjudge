package judge.remote.querier;

import judge.BaseJunitTest;
import judge.remote.status.SubmissionRemoteStatus;
import judge.tool.Handler;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * abstract test for which need login to query
 *
 * @author zzzz76
 */
public abstract class AuthenticatedQuerierTest extends BaseJunitTest{
    private final static Logger log = LoggerFactory.getLogger(AuthenticatedQuerierTest.class);

    // 信号量
    private CountDownLatch doneSignal = new CountDownLatch(1);

    @Test
    public abstract void testQuery() throws Exception;

    public Handler<SubmissionRemoteStatus> getHandler() {
        return new Handler<SubmissionRemoteStatus>() {
            @Override
            public void handle(SubmissionRemoteStatus remoteStatus) {
                printRemoteStatus(remoteStatus);
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

    private void printRemoteStatus(SubmissionRemoteStatus remoteStatus) {
        System.err.println(remoteStatus.statusType.name());
        System.err.println(StringUtils.capitalize(remoteStatus.rawStatus));
        System.err.println(remoteStatus.executionTime);
        System.err.println(remoteStatus.executionMemory);
        System.err.println(StringUtils.left(remoteStatus.compilationErrorInfo, 10000));
        System.err.println(remoteStatus.failCase);
    }
}
