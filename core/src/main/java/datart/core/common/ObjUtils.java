package datart.core.common;

import java.util.Objects;

/**
 * @author suxinshuo
 * @date 2025/12/12 10:35
 */
public class ObjUtils {

    public static boolean getBooleanValue(Object value) {
        if (Objects.isNull(value)) {
            return false;
        }
        return Boolean.parseBoolean(value.toString());
    }

}
