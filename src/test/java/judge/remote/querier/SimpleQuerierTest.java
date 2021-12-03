package judge.remote.querier;

import judge.BaseJunitTest;
import judge.remote.status.SubmissionRemoteStatus;
import judge.tool.Handler;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * abstract test for direct query
 *
 * @author zzzz76
 */
public abstract class SimpleQuerierTest extends BaseJunitTest {
    private final static Logger log = LoggerFactory.getLogger(BaseJunitTest.class);

    @Test
    public abstract void testQuery() throws Exception;

    protected Handler<SubmissionRemoteStatus> getHandler() {
        return new Handler<SubmissionRemoteStatus>() {
            @Override
            public void handle(SubmissionRemoteStatus remoteStatus) {
                printRemoteStatus(remoteStatus);
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
            }
        };
    }

    protected void terminal() throws Exception {
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
