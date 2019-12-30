package com.zxj.fast_io_core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 继承对象拷贝工具
 * 用反射代替new 和手工复制
 * Created by zhuxiujie on 2017/4/10.
 */
public class ExtendBeanBuildUtil {

    public interface AfterSetDataListener<T> {
        T setData(T result);
    }

    public interface AfterSetListDataListener<T> {
        List<T> setListData(List<T> result);
    }

    public static <Father, Child extends Father> List<Child> buildChild(List<Father> args, Class<Child> childClass) {
        return buildChild(args, childClass, null);
    }

    /**
     * 弃用方法，不建议使用
     *
     * @param args
     * @param childClass
     * @param setDataListener
     * @param <Father>
     * @param <Child>
     * @return
     */
    @Deprecated
    public static <Father, Child extends Father> List<Child> buildChild(List<Father> args, Class<Child> childClass, AfterSetDataListener<Child> setDataListener) {
        if (args == null) {
            return null;
        }
        List<Child> vos = new ArrayList<Child>();
        for (Father arg : args) {
            vos.add(buildChild(arg, childClass, setDataListener));
        }
        return vos;
    }


    public static <Father, Child extends Father> List<Child> buildChildList(List<Father> args, Class<Child> childClass) {
        return buildChildList(args, childClass, null);
    }

    public static <Father, Child extends Father> Child buildChildList(Father arg, Class<Child> childClass, AfterSetListDataListener<Child> afterSetListDataListener) {
        List<Father> fathers = new ArrayList<>();
        fathers.add(arg);
        List<Child> childList = buildChildList(fathers, childClass, afterSetListDataListener);
        if (childList == null || childList.size() != 1) {
            return null;
        } else {
            return childList.get(0);
        }
    }

    public static <Father, Child extends Father> List<Child> buildChildList(List<Father> args, Class<Child> childClass, AfterSetListDataListener<Child> afterSetListDataListener) {
        if (args == null) {
            return null;
        }
        List<Child> vos = new ArrayList<Child>();
        for (Father arg : args) {
            vos.add(buildChild(arg, childClass));
        }
        if (afterSetListDataListener != null) {
            List<Child> newVos = afterSetListDataListener.setListData(vos);
            if (newVos != null) {
                vos = newVos;
            }
        }
        return vos;
    }


    public static <Father, Child extends Father> Child buildChild(Father arg, Class<Child> childClass) {
        return buildChild(arg, childClass, null);
    }


    public static <Father, Child extends Father> List<Father> buildFather(List<Child> args, Class<Father> fatherClass) {
        return buildFather(args, fatherClass, null);
    }

    public static <Father, Child extends Father> List<Father> buildFather(List<Child> args, Class<Father> fatherClass, AfterSetDataListener<Father> setDataListener) {
        if (args == null) {
            return null;
        }
        List<Father> vos = new ArrayList<Father>();
        for (Father arg : args) {
            vos.add(buildFather(arg, fatherClass, setDataListener));
        }
        return vos;
    }

    public static <Father, Child extends Father> Father buildFather(Child arg, Class<Father> fatherClass) {
        return buildFather(arg, fatherClass, null);
    }


    public static <Father, Child extends Father> Child buildChild(Father arg, Class<Child> childClass, AfterSetDataListener<Child> setDataListener) {
        if (arg == null) {
            return null;
        }
        Child child = null;
        try {
            child = childClass.newInstance();
        } catch (Exception e) {
            return null;
        }
        child = (Child) BeanUtils.convertToSubclass(arg, child);
        if (setDataListener != null) {
            Child childNew = setDataListener.setData(child);
            if (childNew != null) child = childNew;
        }
        return child;
    }

    public static <Father, Child extends Father> Father buildFather(Child arg, Class<Father> fatherClass, AfterSetDataListener<Father> setDataListener) {
        if (arg == null) {
            return null;
        }
        Father father = null;
        try {
            father = fatherClass.newInstance();
        } catch (Exception e) {
            return null;
        }
        father = (Child) BeanUtils.convertToSubclass(arg, father);
        if (setDataListener != null) {
            Father fatherNew = setDataListener.setData(father);
            if (fatherNew != null) father = fatherNew;
        }
        return father;
    }
}
