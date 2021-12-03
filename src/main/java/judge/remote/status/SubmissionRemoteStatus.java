package judge.remote.status;

import java.util.Date;

public class SubmissionRemoteStatus {
    
    public RemoteStatusType statusType;
    
    public String rawStatus; // 原生状态
    
    /**
     * millisecond
     */
    public int executionTime;
    
    /**
     * KiloBytes
     */
    public int executionMemory;
    
    public String compilationErrorInfo;
    
    public int failCase = -1;
    
    public Date queryTime = new Date();

}
