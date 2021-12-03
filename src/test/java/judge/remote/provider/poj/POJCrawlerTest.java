package judge.remote.provider.poj;

import judge.remote.crawler.SimpleCrawlerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对北京大学题库爬取器进行测试
 *
 * @author zzzz76
 */
public class POJCrawlerTest extends SimpleCrawlerTest {

    @Autowired
    private POJCrawler crawler;

    @Override
    public void testCrawl() throws Exception {
        crawler.crawl("1000", getHandler());
        terminal();
    }

}
