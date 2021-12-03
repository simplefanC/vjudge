package judge.action.response;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-07-30
 */
public class QueryResult extends BaseResult {
    private String status;
    private int time;
    private int memory;
    private String additionalInfo;

    public String getStatus() {
        return status;
    }

    public QueryResult setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getTime() {
        return time;
    }

    public QueryResult setTime(int time) {
        this.time = time;
        return this;
    }

    public int getMemory() {
        return memory;
    }

    public QueryResult setMemory(int memory) {
        this.memory = memory;
        return this;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public QueryResult setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }
}
