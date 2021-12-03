package judge.action.response;

import java.util.Map;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-07-26
 */
public class CrawlResult extends BaseResult{
    private int id;            //Hibernate统编ID
    private String title;        //标题
    private String description;    //题面描述
    private String input;        //输入介绍
    private String output;        //输出介绍
    private String sampleInput;    //样例输入
    private String sampleOutput;//样例输出
    private String hint;        //提示
    private String source;        //出处
    private int timeLimit;        //时间限制(ms)(1:crawling 2:crawl failed)
    private int memoryLimit;    //内存限制(KB)
    private Map<String, String> languageList;

    public int getId() {
        return id;
    }

    public CrawlResult setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CrawlResult setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CrawlResult setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getInput() {
        return input;
    }

    public CrawlResult setInput(String input) {
        this.input = input;
        return this;
    }

    public String getOutput() {
        return output;
    }

    public CrawlResult setOutput(String output) {
        this.output = output;
        return this;
    }

    public String getSampleInput() {
        return sampleInput;
    }

    public CrawlResult setSampleInput(String sampleInput) {
        this.sampleInput = sampleInput;
        return this;
    }

    public String getSampleOutput() {
        return sampleOutput;
    }

    public CrawlResult setSampleOutput(String sampleOutput) {
        this.sampleOutput = sampleOutput;
        return this;
    }

    public String getHint() {
        return hint;
    }

    public CrawlResult setHint(String hint) {
        this.hint = hint;
        return this;
    }

    public String getSource() {
        return source;
    }

    public CrawlResult setSource(String source) {
        this.source = source;
        return this;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public CrawlResult setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
        return this;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public CrawlResult setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
        return this;
    }

    public Map<String, String> getLanguageList() {
        return languageList;
    }

    public CrawlResult setLanguageList(Map<String, String> languageList) {
        this.languageList = languageList;
        return this;
    }
}
