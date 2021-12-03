package judge.action.response;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-07-27
 */
public class SubmitResult extends BaseResult {
    private int taskId;

    public int getTaskId() {
        return taskId;
    }

    public SubmitResult setTaskId(int taskId) {
        this.taskId = taskId;
        return this;
    }
}
