package judge.remote.provider.hdu;

import judge.remote.RemoteOj;
import judge.remote.querier.SimpleQuerierTest;
import judge.remote.submitter.SubmissionInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对杭电题库轮询器进行测试
 *
 * @author zzzz76
 */
public class HDUQuerierTest extends SimpleQuerierTest {

    @Autowired
    private HDUQuerier querier;

    @Override
    public void testQuery() throws Exception {
        querier.query(fakeSubmissionInfo(), getHandler());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remoteOj = RemoteOj.HDU;
        info.remoteProblemId = "1000";
        info.remoteRunId= "35385925";
        info.remoteAccountId = "ecnuv1";
        return info;
    }
}
