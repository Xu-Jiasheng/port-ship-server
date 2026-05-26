package cn.edu.seig.portalship.util;

public class TypeConversionUtil {

    public static Long toLong(Object obj) {
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else {
            throw new IllegalArgumentException("转换失败，不支持类型：" + obj.getClass());
        }
    }
}
