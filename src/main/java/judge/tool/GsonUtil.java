package judge.tool;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-07-14
 */
public class GsonUtil {
    private static Gson gson = null;
    private static JsonParser parser = new JsonParser();

    static {
        if (gson == null) {
            gson = new GsonBuilder()//建造者模式设置不同的配置
//                .serializeNulls()//序列化为null对象
                    .setDateFormat("yyyy-MM-dd HH:mm:ss") //设置日期的格式
//                .disableHtmlEscaping()//防止对网址乱码 忽略对特殊字符的转换
                    .create();
        }
    }

    /**
     * 对象转成json
     *
     * @param object
     * @return json
     */
    public static String toJson(Object object) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }

    /**
     * Json转成对象
     *
     * @param gsonString
     * @param cls
     * @return 对象
     */
    public static <T> T jsonToObject(String gsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }
        return t;
    }

    /**
     * json转成list<T>
     *
     * @param gsonString
     * @param cls
     * @return list<T>
     */
    public static <T> List<T> jsonToList(String gsonString, Class<T> cls) {
        List<T> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString, new TypeToken<List<T>>() {
            }.getType());
        }
        return list;
    }

    /**
     * json转成list中有map的
     *
     * @param gsonString
     * @return List<Map<String, T>>
     */
    public static <T> List<Map<String, T>> jsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString, new TypeToken<List<Map<String, T>>>() {
            }.getType());
        }
        return list;
    }

    /**
     * json转成map的
     *
     * @param gsonString
     * @return Map<String, T>
     */
    public static <T> Map<String, T> jsonToMaps(String gsonString) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }

    private JsonObject object;

    public GsonUtil() {
    }

    public GsonUtil(String gsonString) {
        this.object = parser.parse(gsonString).getAsJsonObject();
    }

    /**
     * 获取json字符串成员
     *
     * @param finalName
     * @param ObjectNames
     * @return
     */
    public String getStrMem(String finalName, String... ObjectNames) {
        JsonObject tmpObject = object;
        for (String objectName: ObjectNames) {
            tmpObject = tmpObject.getAsJsonObject(objectName);
        }
        JsonElement element = tmpObject.get(finalName);
        if (element == null || element.isJsonNull()) {
            return null;
        } else if (element.isJsonObject() || element.isJsonArray()) {
            return element.toString();
        }
        return element.getAsString();
    }

}
