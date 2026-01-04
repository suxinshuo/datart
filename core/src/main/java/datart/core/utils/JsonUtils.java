package datart.core.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;

/**
 * @author suxinshuo
 * @date 2025/12/26 15:09
 */
public class JsonUtils {

    public static String toJsonStr(Object obj) {
        JSONConfig config = JSONConfig.create()
                .setIgnoreNullValue(false)
                .setDateFormat("yyyy-MM-dd HH:mm:ss");
        return JSONUtil.toJsonStr(obj, config);
    }

    public static <T> T toBean(String jsonString, Class<T> beanClass) {
        return JSONUtil.toBean(jsonString, beanClass);
    }

    public static <T> T toBean(String jsonString, TypeReference<T> typeReference, boolean ignoreError) {
        return JSONUtil.toBean(jsonString, typeReference, ignoreError);
    }

}
