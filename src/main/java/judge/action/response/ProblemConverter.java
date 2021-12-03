package judge.action.response;

import judge.bean.Description;
import judge.bean.Problem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-07-27
 */
public class ProblemConverter {
    public static CrawlResult toResult(Problem problem) {
        List<Description> descriptions = new ArrayList<Description>(problem.getDescriptions());
        Description description = descriptions.get(0);

        CrawlResult crawlResult = new CrawlResult();
        crawlResult.setId(problem.getId())
                .setTitle(problem.getTitle())
                .setDescription(description.getDescription())
                .setInput(description.getInput())
                .setOutput(description.getOutput())
                .setSampleInput(description.getSampleInput())
                .setSampleOutput(description.getSampleOutput())
                .setHint(description.getHint())
                .setSource(problem.getSource())
                .setTimeLimit(problem.getTimeLimit())
                .setMemoryLimit(problem.getMemoryLimit());
        return crawlResult;
    }
}
