package judge.remote.provider.nbut;

import judge.remote.crawler.SimpleCrawlerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对宁波工程学院爬取器进行测试
 *
 * @author zzzz76
 */
public class NBUTCrawlerTest extends SimpleCrawlerTest {

    @Autowired
    private NBUTCrawler crawler;

    @Override
    public void testCrawl() throws Exception {
        crawler.crawl("1000", getHandler());
        terminal();
    }
}
