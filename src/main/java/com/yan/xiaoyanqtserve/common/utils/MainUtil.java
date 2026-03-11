package com.yan.xiaoyanqtserve.common.utils;

import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 项目通用工具类
 *
 * @author yanchen
 * @since 2025/5/17 20:32
 */
@Slf4j
public class MainUtil {

    /**
     * 向 ConstraintValidatorContext 添加字段级校验错误（替换默认错误）
     *
     * @param context      校验上下文
     * @param message      错误消息
     * @param propertyNode 字段名（支持嵌套，如 "credentials[0].identifier"）
     */
    public static void addValidationError(
            ConstraintValidatorContext context,
            String message,
            String propertyNode) {

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyNode)
                .addConstraintViolation();
    }

    /**
     * 使用 codePoints 方法进行准确的字符计数
     * 类似于 JavaScript 的 Array.from().length
     */
    public static int accurateCharCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // 使用 codePoints() 方法，它可以正确处理 Unicode 代理对
        return (int) text.trim().codePoints().count();
    }

    /**
     * 获取对象中为空的属性名
     *
     * @param source 对象
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (isEmpty(srcValue)) {
                // 获取属性值是否为空
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    /**
     * 判断对象是否为空，
     * 判定规则：
     * <p>
     * 1. 如果值为 null，判定为空
     * <p>
     * 2. 如果是 String 类型，去除首尾空格后判断是否为空字符串
     * <p>
     * 3. 如果是 Collection 集合类型（List、Set等），判断是否为空集合
     * <p>
     * 4. 如果是 Map 类型，判断是否为空Map
     * <p>
     * 5. 如果是数组类型，判断数组长度是否为0
     * <p>
     * 6. 其他类型不判定为空
     *
     * @param value 待判断的对象
     * @return true-为空，false-不为空
     */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof Object[]) {
            return ((Object[]) value).length == 0;
        }
        return false;
    }

}
