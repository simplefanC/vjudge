package judge.remote.provider.tkoj;

import judge.remote.crawler.SimpleCrawlerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对TK题库爬取器进行测试
 *
 * @author zzzz76
 */
public class TKOJCrawlerTest extends SimpleCrawlerTest {

    @Autowired
    private TKOJCrawler crawler;

    @Override
    public void testCrawl() throws Exception {
        crawler.crawl("1000", getHandler());
        terminal();
    }
}
