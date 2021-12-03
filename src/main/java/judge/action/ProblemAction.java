package judge.action;

import java.util.*;

import javax.servlet.ServletContext;

import judge.action.response.*;
import judge.bean.*;
import judge.remote.ProblemInfoUpdateManager;
import judge.remote.RunningSubmissions;
import judge.remote.SubmitCodeManager;
import judge.remote.language.LanguageManager;
import judge.remote.status.RemoteStatusType;
import judge.tool.OnlineTool;
import judge.tool.Tools;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;


/**
 * 处理 题库/练习 前端相关功能
 * @author Isun
 *
 */
@SuppressWarnings("unchecked")
public class ProblemAction extends BaseAction{
    private final static Logger log = LoggerFactory.getLogger(ProblemAction.class);

    private static final long serialVersionUID = 5557740709776919006L;
    private int id;    //problemId
    private int uid;
    private int isOpen;
    private int res;    //result
    private String OJId;
    private String probNum;
    private String title;
    private Problem problem;
    private Description description;
    private Submission submission;
    private List dataList;
    private String language;
    private String source;
    private String redir;
    private String un;
    private String _64Format;
    private Integer isSup;
    private DataTablesPage dataTablesPage;
    private Map<String, String> languageList;
    private String submissionInfo;
    private Response response;

    @Autowired
    private ProblemInfoUpdateManager problemInfoUpdateManager;

    @Autowired
    private SubmitCodeManager submitManager;

    @Autowired
    private RunningSubmissions runningSubmissions;

    @Autowired
    private LanguageManager languageManager;

    // 罗列题目 get /toListProblem.action
    public String toListProblem() {
        Map session = ActionContext.getContext().getSession();
        if (session.containsKey("error")){
            this.addActionError((String) session.get("error"));
        }
        session.remove("error");
        return SUCCESS;// 转发list.jsp --> post /listProblem.action
    }
    // 题目分页 post /listProblem.action
    public String listProblem() {
        String sortDir = getParameter("order[0][dir]");
        String sortCol = getParameter("order[0][column]");// 按第一列排序
        String start = getParameter("start");// 起始位子，如第一页就从
        String length = getParameter("length");// 预读长度= 预读页数*每页行数
        String draw = getParameter("draw");// 浏览器cache的编号，递增不可重复

        Map<String, String> params = new HashMap<>();
        params.put("sortDir", sortDir);
        params.put("sortCol", sortCol);
        params.put("start", start);
        params.put("length", length);
        params.put("draw", draw);

        // (Re)crawl the problem if necessary
        if (OJListLiteral.contains(OJId) && !StringUtils.isBlank(probNum)) {
            problemInfoUpdateManager.updateProblem(OJId, probNum, false);
        }

        Map session = ActionContext.getContext().getSession();
        StringBuffer hql = new StringBuffer(
                "select "
                        + "  problem.originOJ, "
                        + "  problem.originProb, "
                        + "  problem.title, "
                        + "  problem.triggerTime, "
                        + "  problem.source, "
                        + "  problem.id, "
                        + "  problem.id, "
                        + "  problem.timeLimit "
                        + "from "
                        + "  Problem problem "
                        + "where "
                        + "  problem.title != 'N/A' ");
        long cnt = baseService.count(hql.toString());
        dataTablesPage = new DataTablesPage();
        dataTablesPage.setRecordsTotal(cnt);
        // 关键字筛选
        if (OJListLiteral.contains(OJId)){
            hql.append(" and problem.originOJ = '" + OJId + "' ");
        }
        Map<String, String> paraMap = new HashMap<String, String>();
        if (!StringUtils.isBlank(probNum)) {
            paraMap.put("probNum", "%" + probNum + "%");
            hql.append(" and problem.originProb like :probNum ");
        }
        if (!StringUtils.isBlank(title)) {
            paraMap.put("title", "%" + title + "%");
            hql.append(" and problem.title like :title ");
        }
        if (!StringUtils.isBlank(source)) {
            paraMap.put("source", "%" + source + "%");
            hql.append(" and problem.source like :source ");
        }
        dataTablesPage.setRecordsFiltered(baseService.count(hql.toString(), paraMap));
        // 关键字排序
        if (sortCol != null){
            if (!"desc".equals(sortDir)) {
                sortDir = "";
            }
            if ("1".equals(sortCol)){
                hql.append(" order by problem.originProb " + sortDir);
            } else if ("2".equals(sortCol)){
                hql.append(" order by problem.title " + sortDir);
            } else if ("3".equals(sortCol)){
                hql.append(" order by problem.triggerTime " + sortDir + ", problem.originProb " + sortDir);
            } else if ("4".equals(sortCol)){
                hql.append(" order by problem.source " + sortDir);
            }
        }
        List<Object[]> data = baseService.list(hql.toString(), paraMap, Integer.parseInt(start), Integer.parseInt(length));
        for (Object[] o : data) {
            o[3] = ((Date)o[3]).getTime();
        }
        dataTablesPage.setData(data);
        dataTablesPage.setDraw(Integer.parseInt(draw));

        if (session.containsKey("error")){
            this.addActionError((String) session.get("error"));
        }
        session.remove("error");

        return SUCCESS;// 返回(json)dataTablesPage数据
    }
    // 添加题目 get /recrawlProblem.action
    public String recrawlProblem() {
        problemInfoUpdateManager.updateProblem(OJId, probNum, true);
        return SUCCESS;// 重定向 /viewProblem.action?OJId=${OJId}&probNum=${probNum}
    }
    // 显示题目 get /viewProblem.action
    public String viewProblem(){
        if (!StringUtils.isBlank(OJId) && !StringUtils.isBlank(probNum)) {
            problem = judgeService.findProblem(OJId, probNum);
        } else {
            // 根据唯一标识从数据库获取题目
            List<Problem> list = baseService.query("select p from Problem p left join fetch p.descriptions where p.id = " + id);
            problem = list.get(0);
        }
        _64Format = lf.get(problem.getOriginOJ());
        problemInfoUpdateManager.updateProblem(problem, false); // 更新判定 -> 开启了异步线程
        return SUCCESS;// 转发view.jsp --> 异步快则主线程直接输出，异步过慢则自动刷新
    }

    /**
     * 抓取题目api
     * 输入 id  即可发起一个查询线程（自带session_id）
     */
    public String crawlProblem() {
        if (!StringUtils.isBlank(OJId) && !StringUtils.isBlank(probNum)) {
            problem = judgeService.findProblem(OJId, probNum);
        } else {
            List<Problem> list = baseService.query("select p from Problem p left join fetch p.descriptions where p.id = " + id);
            problem = list.get(0);
        }

        response = new Response();
        if (problem == null) {
            response.setMessage("抓取对象不存在");
            response.setCode(200);
            return SUCCESS;
        }
        problemInfoUpdateManager.updateProblem(problem, false); // 若判定需更新，则开启异步线程

        CrawlResult result = ProblemConverter.toResult(problem);
        result.setLanguageList(languageManager.getLanguages(problem.getOriginOJ(),problem.getOriginProb()));
        response.setBaseResult(result);

        if (result.getTimeLimit() == 1) {
            // 非最终态
            response.setMessage("抓取对象正在更新...");
            response.setCode(100);
        } else if (result.getTimeLimit() == 2) {
            // 最终态 不可用
            response.setMessage("抓取对象不完整");
            response.setCode(200);
        } else {
            // 最终态 可用
            response.setMessage("抓取成功");
            response.setCode(0);
        }
        return SUCCESS;
    }

    /**
     * 查询状态api
     * 输入 id 即可发起一个查询线程 (自带session_id)
     */
    public String queryStatus() {
        submission = (Submission) baseService.query(Submission.class, id);
        response = new Response();
        if (submission == null) {
            // 添加信息
            response.setMessage("The query doesn't exist");
            response.setCode(200);
            return SUCCESS;
        }
        QueryResult result = new QueryResult();
        result.setStatus(submission.getStatusCanonical()).setTime(submission.getTime())
                .setMemory(submission.getMemory()).setAdditionalInfo(submission.getAdditionalInfo());
        response.setBaseResult(result);
        response.setMessage("Success");
        response.setCode(0);
        return SUCCESS;
    }

    /**
     * 提交解答api
     * 输入 id,language,source 即可发起一个提交线程（自带session_id）
     */
    public String submitSolution() throws Exception{
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        response = new Response();
        if (user == null){
            response.setMessage("Please login first!");
            response.setCode(101);
            return SUCCESS;
        }
        problem = (Problem) baseService.query(Problem.class, id);// 从数据库中获取problem bean
        languageList = languageManager.getLanguages(problem.getOriginOJ(),problem.getOriginProb());// 从三方实例中获取语言列表

        // 若提交不规范则重新提交
        if (problem == null){
            response.setMessage("Please submit via normal approach!");
            response.setCode(200);
            return SUCCESS;
        }
        if (problem.getTimeLimit() == 1 || problem.getTimeLimit() == 2){
            response.setMessage("Crawling has not finished!");
            response.setCode(200);
            return SUCCESS;
        }

        if (!languageList.containsKey(language)){
            response.setMessage("No such a language!");
            response.setCode(200);
            return SUCCESS;
        }
        source = new String(Base64.decodeBase64(source), "utf-8");
        if (source.length() < 50){
            response.setMessage("Source code should be longer than 50 characters!");
            response.setCode(200);
            return SUCCESS;
        }
        if (source.getBytes("utf-8").length > 30000){
            response.setMessage("Source code should be shorter than 30000 bytes in UTF-8!");
            response.setCode(200);
            return SUCCESS;
        }
        submission = new Submission(); // 创建submission
        submission.setSubTime(new Date());
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setStatus("Pending");
        submission.setStatusCanonical(RemoteStatusType.PENDING.name());
        submission.setLanguage(language);
        submission.setSource(source);
        submission.setIsOpen(user.getShare());
        submission.setDispLanguage(languageManager.getLanguages(problem.getOriginOJ(),problem.getOriginProb()).get(language));
        submission.setUsername(user.getUsername());
        submission.setOriginOJ(problem.getOriginOJ());
        submission.setOriginProb(problem.getOriginProb());
        submission.setLanguageCanonical(Tools.getCanonicalLanguage(submission.getDispLanguage()).toString());
        baseService.addOrModify(submission);// 数据库添加submission bean
        System.out.println(submission.getStatus());

        submitManager.submitCode(submission);// 进行判题
        response.setMessage("Submitting in process...");
        response.setCode(0);

        // 提交结束后，前端应该要获取啥
        SubmitResult result = new SubmitResult();
        result.setTaskId(submission.getId());
        response.setBaseResult(result);
        return SUCCESS;
    }

    // 为描述投票 post /vote4Description.action
    public String vote4Description(){
        Map session = ActionContext.getContext().getSession();
        Set votePids = (Set) session.get("votePids");
        if (votePids == null){
            votePids = new HashSet<Integer>();
            session.put("votePids", votePids);
        }
        Description desc = (Description) baseService.query(Description.class, id);
        desc.setVote(desc.getVote() + 1);
        baseService.addOrModify(desc);
        votePids.add(desc.getProblem().getId());
        return SUCCESS;// 无
    }
    // 进入非比赛提交页面 get /toSubmit.action
    public String toSubmit(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        if (user == null){
            //            session.put("redir", "../problem/toSubmit.action?id=" + id);
            return ERROR;// 重定向 /toIndex.action
        }
        ServletContext sc = ServletActionContext.getServletContext();
        problem = (Problem) baseService.query(Problem.class, id);
        if (problem == null) {
            return ERROR;
        }

        languageList = languageManager.getLanguages(problem.getOriginOJ(),problem.getOriginProb());
        //        languageList = (Map<Object, String>) sc.getAttribute(problem.getOriginOJ());
        isOpen = user.getShare();
        return SUCCESS;// 转发submit.jsp
    }
    // 非比赛中的提交 post /submit.action
    public String submit() throws Exception{
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        if (user == null){
            return ERROR;// 重定向 /toIndex.action
        }
        problem = (Problem) baseService.query(Problem.class, id);// 从数据库中获取problem bean
        //        ServletContext sc = ServletActionContext.getServletContext();
        //        languageList = (Map<Object, String>) sc.getAttribute(problem.getOriginOJ());
        languageList = languageManager.getLanguages(problem.getOriginOJ(),problem.getOriginProb());// 从三方实例中获取语言列表

        // 若提交不规范则重新提交
        if (problem == null){
            this.addActionError("Please submit via normal approach!");
            return INPUT;// 转发submit.jsp
        }
        if (problem.getTimeLimit() == 1 || problem.getTimeLimit() == 2){
            this.addActionError("Crawling has not finished!");
            return INPUT;
        }

        if (!languageList.containsKey(language)){
            this.addActionError("No such a language!");
            return INPUT;
        }
        source = new String(Base64.decodeBase64(source), "utf-8");
        if (source.length() < 50){
            this.addActionError("Source code should be longer than 50 characters!");
            return INPUT;
        }
        if (source.getBytes("utf-8").length > 30000){
            this.addActionError("Source code should be shorter than 30000 bytes in UTF-8!");
            return INPUT;
        }
        submission = new Submission(); // 创建submission
        submission.setSubTime(new Date());
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setStatus("Pending");//Pending状态
        submission.setStatusCanonical(RemoteStatusType.PENDING.name());
        submission.setLanguage(language);
        submission.setSource(source);
        submission.setIsOpen(isOpen);
        submission.setDispLanguage(
                languageManager.getLanguages(problem.getOriginOJ(), problem.getOriginProb())
                        .get(language));
        submission.setUsername(user.getUsername());
        submission.setOriginOJ(problem.getOriginOJ());
        submission.setOriginProb(problem.getOriginProb());
        submission.setLanguageCanonical(Tools.getCanonicalLanguage(submission.getDispLanguage()).toString());
        baseService.addOrModify(submission);// 数据库添加submission bean 然后 id会回写到submission
        System.out.println(submission.getStatus());
        if (user.getShare() != submission.getIsOpen()) {
            user.setShare(submission.getIsOpen());
            baseService.addOrModify(user);
        }

        submitManager.submitCode(submission);// 进行判题
        return SUCCESS;// 重定向 /status.action
    }
    // 显示提交 get /status.action
    public String status() {
        if (id != 0){
            problem = (Problem) baseService.query(Problem.class, id);
            OJId = problem.getOriginOJ();
            probNum = problem.getOriginProb();
        }
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        isSup = user == null ? 0 : user.getSup();

        if (session.containsKey("error")){
            this.addActionError((String) session.get("error"));
        }
        session.remove("error");

        return SUCCESS;// 转发status.jsp
    }
    // 提交分页 post /fetchStatus.action
    public String fetchStatus() {
        String start = getParameter("start");
        String length = getParameter("length");
        String draw = getParameter("draw");
        String orderBy = getParameter("orderBy");

        Map session = ActionContext.getContext().getSession();
        Map paraMap = new HashMap();
        User user = (User) session.get("visitor");
        int userId = user != null ? user.getId() : -1;
        int sup = user != null ? user.getSup() : 0;

        StringBuffer hql = new StringBuffer("" +
                "select " +
                "  s.id, " + // 0
                "  s.username, " +
                "  s.problem.id, " +
                "  s.status, " +
                "  s.memory, " +
                "  s.time, " + // 5
                "  s.dispLanguage, " +
                "  length(s.source), " +
                "  s.subTime, " +
                "  s.user.id, " +
                "  s.isOpen, " + // 10
                "  s.originOJ, " +
                "  s.originProb, " +
                "  s.contest.id, " +
                "  s.additionalInfo, " +
                "  s.statusCanonical, " + // 15
                "  s.id " +
                "from " +
                "  Submission s ");

        dataTablesPage = new DataTablesPage();
        dataTablesPage.setRecordsTotal(9999999L);

        if (sup == 0){
            hql.append("" +
                    "left join " +
                    "  s.contest c " +
                    "where " +
                    "  ((s.isPrivate = 0 or s.user.id = :userId) and" +
                    "  (c is null or c.endTime < :currentTime)) ");
            paraMap.put("currentTime", new Date());
            paraMap.put("userId", user != null ? user.getId() : -1);
        } else {
            hql.append(" where 1 = 1 ");
        }

        if (un != null && !un.trim().isEmpty()){
            hql.append(" and s.username = :un ");
            paraMap.put("un", un.toLowerCase().trim());
        }

        if (!probNum.isEmpty()){
            hql.append(" and s.originProb = :probNum ");
            paraMap.put("probNum", probNum);
        }
        if (OJListLiteral.contains(OJId)){
            hql.append(" and s.originOJ = :OJId ");
            paraMap.put("OJId", OJId);
        }

        if (res == 1){
            hql.append(" and s.statusCanonical = 'AC' ");
        } else if (res == 2) {
            hql.append(" and s.statusCanonical = 'PE' ");
        } else if (res == 3) {
            hql.append(" and s.statusCanonical = 'WA' ");
        } else if (res == 4) {
            hql.append(" and s.statusCanonical = 'TLE' ");
        } else if (res == 5) {
            hql.append(" and s.statusCanonical = 'MLE' ");
        } else if (res == 6) {
            hql.append(" and s.statusCanonical = 'OLE' ");
        } else if (res == 7) {
            hql.append(" and s.statusCanonical = 'RE' ");
        } else if (res == 8) {
            hql.append(" and s.statusCanonical = 'CE' ");
        } else if (res == 9) {
            hql.append(" and s.statusCanonical in ('FAILED_OTHER', 'SUBMIT_FAILED_PERM') ");
        } else if (res == 10) {
            hql.append(" and s.statusCanonical = 'SUBMIT_FAILED_TEMP' ");
        } else if (res == 11) {
            hql.append(" and s.statusCanonical in ('PENDING', 'SUBMITTED', 'QUEUEING', 'COMPILING', 'JUDGING') ");
        }

        if (!StringUtils.isBlank(language)) {
            hql.append(" and s.languageCanonical = :language ");
            paraMap.put("language", language);
        }

        if (OJListLiteral.contains(OJId) && !StringUtils.isBlank(probNum) && 1 == res) {
            if ("memory".equals(orderBy)) {
                hql.append(" order by s.memory asc, s.time asc, length(s.source) asc ");
            } else if ("time".equals(orderBy)) {
                hql.append(" order by s.time asc, s.memory asc, length(s.source) asc ");
            } else if ("length".equals(orderBy)) {
                hql.append(" order by length(s.source) asc, s.time asc, s.memory asc ");
            } else {
                hql.append(" order by s.id desc ");
            }
        } else {
            hql.append(" order by s.id desc ");
        }

        dataTablesPage.setRecordsFiltered(9999999L);

        List<Object[]> aaData = baseService.list(hql.toString(), paraMap, Integer.parseInt(start), Integer.parseInt(length));

        for (Object[] o : aaData) {
            o[8] = ((Date)o[8]).getTime();
            o[10] = (Integer)o[10] > 0 ? 2 : sup > 0 || (Integer)o[9] == userId ? 1 : 0;
            o[14] = o[14] == null ? 0 : 1;

            RemoteStatusType statusType = RemoteStatusType.valueOf((String)o[15]);
            o[15] = statusType == RemoteStatusType.AC ? 0 : statusType.finalized ? 1 : 2; // 0-green 1-red 2-purple

            int submissionId = (Integer) o[0];
            try {
                if (!statusType.finalized) {
                    long freezeThreshold = (statusType == RemoteStatusType.SUBMIT_FAILED_TEMP) ? Math.max(3600000L, System.currentTimeMillis() - (Long)o[8]) : 300000L;
                    long freezeLength = runningSubmissions.getFreezeLength(submissionId);
                    if ((freezeLength != Long.MAX_VALUE || statusType != RemoteStatusType.SUBMIT_FAILED_TEMP) && freezeLength > freezeThreshold) {
                        submission = (Submission) baseService.query(Submission.class, submissionId);
                        judgeService.rejudge(submission, false);
                    }
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }

            o[16] = runningSubmissions.contains(submissionId) ? 1 : 0; // 1-working 0-notWorking
        }

        dataTablesPage.setData(aaData);
        dataTablesPage.setDraw(Integer.parseInt(draw));

        return SUCCESS;// 返回(json)dataTablesPage数据
    }
    // 进入修改题面页面 get /toEditDescription.action
    public String toEditDescription(){
        Map session = ActionContext.getContext().getSession();
        List list = baseService.query("select d from Description d left join fetch d.problem where d.id = " + id);
        description = (Description) list.get(0);
        problem = description.getProblem();
        if (session.get("visitor") == null){
            return "login";// 重定向 /toIndex.action
        }
        redir = ServletActionContext.getRequest().getHeader("Referer") + "&edit=1";
        return SUCCESS;// 转发edit_description.jsp
    }
    // 修改题面 post /editDescription.action
    public String editDescription(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        if (user == null){
            session.put("error", "Please login first!");
            return ERROR;// 无
        }
        if (id == 0){
            return ERROR;
        }
        description.setDescription(Jsoup.clean(description.getDescription(), Whitelist.relaxed()));
        description.setInput(Jsoup.clean(description.getInput(), Whitelist.relaxed()));
        description.setOutput(Jsoup.clean(description.getOutput(), Whitelist.relaxed()));
        description.setSampleInput(Jsoup.clean(description.getSampleInput(), Whitelist.relaxed()));
        description.setSampleOutput(Jsoup.clean(description.getSampleOutput(), Whitelist.relaxed()));
        description.setHint(Jsoup.clean(description.getHint(), Whitelist.relaxed()));

        description.setUpdateTime(new Date());
        description.setAuthor(user.getUsername());
        description.setVote(0);
        description.setProblem(new Problem(id));
        baseService.execute("delete from Description d where d.author = '" + user.getUsername() + "' and d.problem.id = " + id);
        baseService.addOrModify(description);
        return SUCCESS;// 重定向 /${redir}
    }
    // 删除题面 post /deleteDescription.action
    public String deleteDescription(){
        User user = OnlineTool.getCurrentUser();
        if (user == null) {
            return ERROR;// 无
        }
        Session session = baseService.getSession();
        Transaction tx = session.beginTransaction();
        try {
            description = (Description) session.get(Description.class, id);
            if (!description.getAuthor().equals("0") && (user.getSup() == 1 || user.getUsername().equals(description.getAuthor()))){
                Set<Cproblem> cproblems = description.getCproblems();
                if (cproblems.size() > 0) {
                    //需要把引用该描述的cproblem置为引用system crawler对应的描述
                    Set<Description> descriptions = description.getProblem().getDescriptions();
                    Description sysDescription = null;
                    for (Description description : descriptions) {
                        if (description.getAuthor().equals("0")) {
                            sysDescription = description;
                            break;
                        }
                    }
                    for (Cproblem cproblem : cproblems) {
                        cproblem.setDescription(sysDescription);
                    }
                }
                session.delete(description);
            }
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
        } finally {
            baseService.releaseSession(session);
        }
        return SUCCESS;// 无
    }
    // 查看代码 get /viewSource.action
    public String viewSource(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        List list = baseService.query("select s from Submission s left join fetch s.contest left join fetch s.problem left join fetch s.user where s.id = " + id);
        if (list.isEmpty()){
            session.put("error", "No such submission!");
            return ERROR;// 重定向 /status.action
        }
        submission = (Submission) list.get(0);
        if (!(user != null && (user.getSup() != 0 || user.getId() == submission.getUser().getId()) || submission.getIsOpen() == 1 && (submission.getContest() == null || new Date().compareTo(submission.getContest().getEndTime()) > 0))){
            session.put("error", "No access to this code!");
            return ERROR;
        }

        if (submission.getSubTime().getTime() < limitViewSource) {
            session.put("error", "No access to front code, come back next day");
            return ERROR;
        }
        problem = submission.getProblem();
        submission.setSource(Tools.toHTMLChar(submission.getSource()));
        languageList = languageManager.getLanguages(problem.getOriginOJ(),problem.getOriginProb());
        //        languageList = (Map<Object, String>) ApplicationContainer.serveletContext.getAttribute(problem.getOriginOJ());
        submission.setLanguage(languageList.get(submission.getLanguage()));
        uid = submission.getUser().getId();
        un = submission.getUser().getUsername();

        //这里language用作为shjs提供语言识别所需要的class名
        language = Tools.findClass4SHJS(submission.getLanguage());

        return SUCCESS;// 转发source.jsp
    }
    // 重判代码 post /rejudge.action
    public String rejudge(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        List<Submission> submissionList = baseService.query("select s from Submission s left join fetch s.problem where s.id = " + id);
        if (!submissionList.isEmpty()) {
            submission = submissionList.get(0);
        }
        if (submission == null){
            return ERROR;// 无
        } else {
            judgeService.rejudge(submission, false);
            return SUCCESS;// 返回状态200
        }
    }
    // 切花代码公开性 post /toggleOpen.action
    public String toggleOpen() {
        judgeService.toggleOpen(id);
        return SUCCESS;// 无
    }
    // 获取提交信息 get /fetchSubmissionInfo.action
    public String fetchSubmissionInfo() {
        submission = (Submission) baseService.query(Submission.class, id);
        submissionInfo = submission.getAdditionalInfo();
        return SUCCESS;// 返回(json)submissionInfo数据
    }
    // 访问原始链接 get /visitOriginUrl.action
    public String visitOriginUrl() {
        List<String> _url = baseService.query("select p.url from Problem p where p.id = " + id);
        if (_url.isEmpty()) {
            return ERROR;
        } else {
            redir = _url.get(0);
            return SUCCESS;// 重定向 /${redir}
        }
    }

    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    public Submission getSubmission() {
        return submission;
    }
    public void setSubmission(Submission submission) {
        this.submission = submission;
    }
    public int getIsOpen() {
        return isOpen;
    }
    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }

    public List getDataList() {
        return dataList;
    }
    public void setDataList(List dataList) {
        this.dataList = dataList;
    }
    public Map<String, String> getLanguageList() {
        return languageList;
    }
    public void setLanguageList(Map<String, String> languageList) {
        this.languageList = languageList;
    }
    public String getRedir() {
        return redir;
    }
    public void setRedir(String redir) {
        this.redir = redir;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Problem getProblem() {
        return problem;
    }
    public void setProblem(Problem problem) {
        this.problem = problem;
    }
    public String getOJId() {
        return OJId;
    }
    public void setOJId(String id) {
        OJId = id;
    }
    public String getProbNum() {
        return probNum;
    }
    public void setProbNum(String probNum) {
        this.probNum = probNum;
    }
    public DataTablesPage getDataTablesPage() {
        return dataTablesPage;
    }
    public void setDataTablesPage(DataTablesPage dataTablesPage) {
        this.dataTablesPage = dataTablesPage;
    }
    public int getRes() {
        return res;
    }
    public void setRes(int res) {
        this.res = res;
    }
    public String getUn() {
        return un;
    }
    public void setUn(String un) {
        this.un = un;
    }
    public Description getDescription() {
        return description;
    }
    public void setDescription(Description description) {
        this.description = description;
    }
    public String get_64Format() {
        return _64Format;
    }
    public void set_64Format(String _64Format) {
        this._64Format = _64Format;
    }
    //    public String getProbNum1() {
    //        return probNum1;
    //    }
    //    public void setProbNum1(String probNum1) {
    //        this.probNum1 = probNum1;
    //    }
    //    public String getProbNum2() {
    //        return probNum2;
    //    }
    //    public void setProbNum2(String probNum2) {
    //        this.probNum2 = probNum2;
    //    }
    public Integer getIsSup() {
        return isSup;
    }
    public void setIsSup(Integer isSup) {
        this.isSup = isSup;
    }
    public String getSubmissionInfo() {
        return submissionInfo;
    }
    public void setSubmissionInfo(String submissionInfo) {
        this.submissionInfo = submissionInfo;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Response getResponse() {
        return response;
    }
    public void setResponse(Response response) {
        this.response = response;
    }

}
