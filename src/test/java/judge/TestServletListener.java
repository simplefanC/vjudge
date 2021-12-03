package judge;

import judge.tool.ApplicationContainer;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * @author zzzz76
 */
public class TestServletListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
		/* no-op */
        ApplicationContainer.serveletContext = new MockServletContext();
    }
}
