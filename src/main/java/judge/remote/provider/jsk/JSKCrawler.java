package judge.remote.provider.jsk;

import judge.remote.RemoteOjInfo;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.GsonUtil;
import judge.tool.Tools;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-10-03
 */

@Component
public class JSKCrawler extends SimpleCrawler {

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return this.getHost().toURI() + "/t/" + problemId;
    }

    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d*"));
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        String json = Tools.regFind(html, "var problem=(\\{[\\s\\S]*?\\});var").trim();
        GsonUtil gsonUtil = new GsonUtil(json);

        info.title = gsonUtil.getStrMem("title");
        info.timeLimit = Integer.parseInt(gsonUtil.getStrMem("time_limit"));
        info.memoryLimit = Integer.parseInt(gsonUtil.getStrMem("mem_limit"));
        info.description = gsonUtil.getStrMem("description");
        info.sampleInput = "<pre>" + gsonUtil.getStrMem("sample_input") + "</pre>";
        info.sampleOutput = "<pre>" + gsonUtil.getStrMem("sample_output") + "</pre>";
        info.hint = gsonUtil.getStrMem("hint");
        info.source = gsonUtil.getStrMem("source");
    }
}
