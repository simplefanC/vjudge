package judge.remote.provider.jsk;

import judge.remote.crawler.SimpleCrawlerTest;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 对计蒜客爬取器进行测试
 *
 * @author zzzz76
 */
public class JSKCrawlerTest extends SimpleCrawlerTest{

    @Autowired
    private JSKCrawler crawler;

    @Override
    public void testCrawl() throws Exception {
        crawler.crawl("34486", getHandler());
        terminal();
    }
}
