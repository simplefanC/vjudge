package judge.remote.provider.mxt;

import judge.remote.RemoteOj;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SubmitterTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对码学堂提交器进行测试
 *
 * @author zzzz76
 */
public class MXTSubmitterTest extends SubmitterTest {

    @Autowired
    private MXTSubmitter submitter;

    @Override
    public void testSubmitCode() throws Exception {
        submitter.submitCode(fakeSubmissionInfo(), getHandle());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remotelanguage = "1";
        info.remoteOj = RemoteOj.MXT;
        info.remoteProblemId = "1691";
        info.sourceCode =
                "#include <cstdio>\n" +
                "#include <cstdlib>\n" +
                "int main() {\n" +
                "    int a, b, c;\n" +
                "    scanf(\"%d%d%d\", &a, &b ,&c);\n" +
                "    printf(\"%d\\n\", (a+b)*c);\n" +
                "    return EXIT_SUCCESS;\n" +
                "}";
        return info;
    }
}
