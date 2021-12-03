package judge.action.response;


import java.util.List;

import org.apache.struts2.json.annotations.JSON;

public class DataTablesPage {

    private List data;
    private Long recordsTotal;// 原始数据总数
    private Long recordsFiltered;// 过滤后的数据总数
    private Integer draw;// 浏览器cache的编号，递增不可重复


    /**
     * 自定义序列化name
     * @return
     */
    @JSON(name = "recordsTotal")
    public Long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    @JSON(name = "recordsFiltered")
    public Long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    @JSON(name = "data")
    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public Integer getDraw() {
        return draw;
    }

    public void setDraw(Integer draw) {
        this.draw = draw;
    }
    

}
