package com.zxj.fast_io_core.util;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhuxiujie
 */
public final class BeanUtils {

    private static boolean enableLog = false;

    public static <Father, Child extends Father> Object convertToSubclass(Father father,
                                                                          Child child,
                                                                          boolean skipSourceNull,
                                                                          boolean skipTargetHave) {
        List<Field> fields = getFields(father);
        return convert(father, child, fields, skipSourceNull, skipTargetHave);
    }

    public static <Father, Child extends Father> Object convertToSuperclass(Child child,
                                                                            Father father,
                                                                            boolean skipSourceNull,
                                                                            boolean skipTargetHave) {
        List<Field> fields = getFields(father);
        return convert(child, father, fields, skipSourceNull, skipTargetHave);
    }

    public static <Father, Child extends Father> Object convertToSubclass(Father father, Child child) {
        List<Field> fields = getFields(father);
        return convert(father, child, fields, false, false);
    }

    public static <Father, Child extends Father> Object convertToSuperclass(Child child, Father father) {
        List<Field> fields = getFields(child);
        return convert(child, father, fields, false, false);
    }


    public static Object staticInvoke(Class<?> fromClass, String mothed, Class<?>[] parameterTypes, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = fromClass.getMethod(mothed, parameterTypes);
        return method.invoke(null, args);
    }

    public static Object invoke(Object fromObject, String mothed, Class<?>[] parameterTypes, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (fromObject == null) return null;
        Method method = fromObject.getClass().getMethod(mothed, parameterTypes);
        return method.invoke(fromObject, args);
    }

    /**
     * 使用get set 方法拷贝
     *
     * @param source
     * @param target
     * @param skipSourceNull
     * @param skipTargetHave
     * @return
     */
    public static <T> T fieldsCopy(T source, T target, boolean skipSourceNull, boolean skipTargetHave) {
        List<Field> fields = getFields(target);
        return (T) convert(source, target, fields, skipSourceNull, skipTargetHave);
    }


    /**
     * @param source
     * @param name
     * @return
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static Object getPrivateProperty(Object source, String name) throws IllegalAccessException, NoSuchFieldException {
        // /通过类的字节码得到该类中声明的所有属性，无论私有或公有
        Field[] fields = source.getClass().getDeclaredFields();
        Field field = source.getClass().getDeclaredField(name);
        // 设置访问权限（这点对于有过android开发经验的可以说很熟悉）
        field.setAccessible(true);
        // 得到私有的变量值
        Object result = field.get(source);
        // 输出私有变量的值
        return result;
    }

    /**
     * 根据fieldName 取对象属性
     *
     * @param source
     * @param field
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object getProperty(Object source, Field field)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        if (source == null || field == null)
            return null;
        field.setAccessible(true);
        return field.get(source);
    }

    /**
     * 根据fieldName 取对象属性
     *
     * @param source
     * @param fieldName
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object getProperty(Object source, String fieldName) throws  IllegalAccessException {
        if (source == null) return null;
        // 获得指定类的属性
        Field field = getField(source, fieldName);
        // 值为 true 则指示反射的对象在使用时应该取消 Java 语言访问检查。值为 false 则指示反射的对象应该实施 Java 语言访问检查。
        if (field == null) return null;
        field.setAccessible(true);
        // 更改私有属性的值
        return field.get(source);
    }

    /**
     * 取出深层属性值，用.隔开各个属性。
     *
     * @param source
     * @param fieldName 单层属性取值"a",多层取"a.b.c" 其中abc 都为属性值小写驼峰
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object getPropertys(Object source, String fieldName) throws  IllegalAccessException {
        String[] fieldNames = fieldName.split("\\.");
        Object obj = source;
        for (int i = 0; i < fieldNames.length; i++) {
            if (i == 0) {
                obj = BeanUtils.getProperty(obj, fieldNames[i]);
            } else {
                obj = BeanUtils.getProperty(obj, fieldNames[i]);
            }
            if (obj == null) return obj;
        }
        return obj;
    }

    /**
     * convert source to target, copy source field value
     *
     * @param source
     * @param target
     * @param skipSourceNull
     * @param skipTargetHave
     * @return
     */
    private static Object convert(Object source,
                                  Object target,
                                  List<Field> fields,
                                  boolean skipSourceNull,
                                  boolean skipTargetHave) {

        if (enableLog) System.out.println("fields:" + fields.size() + ",name=" + source.getClass().getName());
        if (fields != null) for (int i = 0; i < fields.size(); i++) {
            try {
                Field field = fields.get(i);
                // ignore static final field, such as serialVersionUID
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                    // 不使用符合JavaBean规范的属性访问器
                    Object obj = getProperty(source, field.getName());
                    if (skipSourceNull && obj == null) {
                        continue;
                    }
                    Object targetObj = getProperty(target, field.getName());
                    if (skipTargetHave && targetObj != null) {
                        continue;
                    }
                    // 调用setter
                    setProperty(target, field.getName(), obj);
                    if (enableLog) System.out.println("name:" + field.getName() + ",obj:" + obj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return target;
    }

    public static void setProperty(Object object, String propertyName, Object value) throws IllegalArgumentException, IllegalAccessException {
        if (object == null||propertyName==null)
            return;
        // 获得指定类的属性
        Field field = getField(object, propertyName);
        setProperty(object,field,value);
    }

    public static void setProperty(Object object,Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
        if (object==null||field == null)
            return;
        // 值为 true 则指示反射的对象在使用时应该取消 Java 语言访问检查。值为 false 则指示反射的对象应该实施 Java 语言访问检查。
        if (field != null ) {
            field.setAccessible(true);
            // 更改私有属性的值
            field.set(object, value);
        }
    }


    public static List<Field> getFields(Object source) {
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = source.getClass();
        while (tempClass != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
        for (Field field:fieldList){
            field.setAccessible(true);
        }
        return fieldList;
    }

    /**
     * getField
     * @param source class or object
     * @param name
     * @return
     */
    public static Field getField(Object source, String name) {
       Class tempClass = source.getClass();
        while (tempClass != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            Field field = null;
            try {
                field = tempClass.getDeclaredField(name);
                if (field != null){
                    field.setAccessible(true);
                    return field;
                }
            } catch (Exception e) {
            }
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
        return null;
    }

    /**
     * 用于使用反射 取className 使pojo set 一个 object
     *
     * @param source   source 包含 target
     * @param target
     * @param <SOURCE>
     * @param <TARGET>
     * @throws Exception
     */
    public static <SOURCE, TARGET> void invokeSetData(SOURCE source, TARGET target) throws Exception {
        invokeSetData(source, target, null);
    }

    /**
     * 用于使用反射 取className 使pojo set 一个 object
     *
     * @param source   source 包含 target
     * @param target
     * @param <SOURCE>
     * @param <TARGET>
     * @throws Exception
     */
    public static <SOURCE, TARGET> void invokeSetData(SOURCE source, TARGET target, String propertyName) throws Exception {
        if (propertyName == null) {
            propertyName = target.getClass().getSimpleName();
        }
        String name = captureName(propertyName);
        // 获得指定类的属性
        Field field = getField(source, name);
        // 值为 true 则指示反射的对象在使用时应该取消 Java 语言访问检查。值为 false 则指示反射的对象应该实施 Java 语言访问检查。
        field.setAccessible(true);
        // 更改私有属性的值
        field.set(source, target);
    }

    public static String captureName(String name) {
        if (name.length() > 1) name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return name;
    }

    public static void setEnableLog(boolean enableLog) {
        BeanUtils.enableLog = enableLog;
    }
}
