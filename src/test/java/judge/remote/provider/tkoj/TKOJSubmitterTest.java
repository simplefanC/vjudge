package judge.remote.provider.tkoj;

import judge.remote.RemoteOj;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SubmitterTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对TK题库提交器进行测试
 *
 * @author zzzz76
 */
public class TKOJSubmitterTest extends SubmitterTest {
    @Autowired
    private TKOJSubmitter submitter;

    @Override
    public void testSubmitCode() throws Exception {
        submitter.submitCode(fakeSubmissionInfo(), getHandle());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remotelanguage = "1";
        info.remoteOj = RemoteOj.TKOJ;
        info.remoteProblemId = "1000";
        info.sourceCode =
                "#include <cstdio>\n" +
                "#include <cstdlib>\n" +
                "int main() {\n" +
                "    int a, b;\n" +
                "    while(scanf(\"%d%d\", &a, &b) != EOF) {\n" +
                "        printf(\"%d\\n\", a + b);\n" +
                "    }\n" +
                "    return 0;\n" +
                "}";
        return info;
    }
}
