package judge.remote.submitter;

import judge.BaseJunitTest;
import judge.tool.Handler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * abstract test for submit
 *
 * @author zzzz76
 */
public abstract class SubmitterTest extends BaseJunitTest {
    private final static Logger log = LoggerFactory.getLogger(SubmitterTest.class);

    // 信号量
    private CountDownLatch doneSignal = new CountDownLatch(1);

    @Test
    public abstract void testSubmitCode() throws Exception;

    protected Handler<SubmissionReceipt> getHandle() {
        return new Handler<SubmissionReceipt>() {
            @Override
            public void handle(SubmissionReceipt receipt) {
                printReceipt(receipt);
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

    private void printReceipt(SubmissionReceipt receipt) {
        System.err.println(receipt.remoteRunId);
        System.err.println(receipt.remoteAccountId);
        System.err.println(receipt.errorStatus);
        System.err.println(receipt.submitTime);
    }
}
