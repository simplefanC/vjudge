package judge.remote.provider.jsk;

import judge.remote.RemoteOj;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SubmitterTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对计蒜客提交器进行测试
 *
 * @author zzzz76
 */
public class JSKSubmitterTest extends SubmitterTest {

    @Autowired
    private JSKSubmitter submitter;

    @Override
    public void testSubmitCode() throws Exception {
        submitter.submitCode(fakeSubmissionInfo(), getHandle());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remotelanguage = "c";
        info.remoteOj = RemoteOj.JSK;
        info.remoteProblemId = "34486";
        info.sourceCode =
                "#include <stdio.h>\n" +
                "int main() {\n" +
                "    int A, B;\n" +
                "    scanf(\"%d %d\", &A, &B);\n" +
                "    printf(\"%d\\n\", A + B);\n" +
                "    return 0;\n" +
                "}";
        return info;
    }

}
