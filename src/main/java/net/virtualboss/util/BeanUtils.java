package net.virtualboss.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@UtilityClass
public class BeanUtils {

    @SneakyThrows
    public static void copyNonNullProperties(Object from, Object to) {
        Class<?> clazz = from.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field f : fields) {
            ReflectionUtils.makeAccessible(f);
            Object value = ReflectionUtils.getField(f, from);
            if (value != null) {
                ReflectionUtils.setField(f, to, value);
            }
        }
    }
}