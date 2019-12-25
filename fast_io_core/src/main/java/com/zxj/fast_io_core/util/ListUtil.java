package com.zxj.fast_io_core.util;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * list 工具集
 * 用于解决 查询的延迟(latency)
 * 传统的查询5个 用户 List<UserVO> 需要加上查询5个用户的资产 List<PropertyVO>,如果是逐个查询的话，网络延迟(latency) 假设为50ms，查询5次耗时5*50=250ms，
 * 在数据量大的情况下 更加惨不忍睹。因此代码需要一个工具 处理2个批量查询出的数据，整合 List<UserVO> List<PropertyVO>，以及更多的数据。
 * <p>
 * Created by zhuxiujie on 2017/9/26.
 */
public class ListUtil {

    public interface Transfer<Source, Result> {
        Result transfer(Source t);
    }

    public interface AfterSetDataListener<Source, Data> {
        void setData(Source t, Data data);
    }

    public interface EndForEach<Source> {
        void end(Source source);
    }

    public interface CompareDataSuccess<SourceData, Data> {
        /**
         * 数据对比成功执行
         *
         * @return
         */
        void compareDataSuccess(SourceData t, Data data);
    }

    public interface IntegerTransfer<Source> extends Transfer<Source, Integer> {
        /**
         * 取Integer 值
         *
         * @param source
         * @return
         */
        Integer transfer(Source source);
    }

    public interface StringTransfer<Source> extends Transfer<Source, String> {
        /**
         * 取Integer 值
         *
         * @param source
         * @return
         */
        String transfer(Source source);
    }

    /**
     * 对象取 比较的key
     *
     * @param <KeyType>
     * @param <SourceData>
     * @param <TargetData>
     */
    public interface ObjectKeyTransfer<KeyType, SourceData, TargetData> {

        KeyType getSourceKey(SourceData f);

        KeyType getTargetKey(TargetData t);
    }

    @Documented
    @Inherited
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ListCompareSetData {
        ListCompareSetDataItem[] keys();
    }

    @Documented
    @Inherited
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ListCompareSetDataItem {

        /**
         * 本地属性判断依据key，用于目标key对比
         *
         * @return
         */
        String localPropertyKey();

        /**
         * 目标对象属性庞大依据key，用于本地key对比
         *
         * @return
         */
        String targetPropertyKey();

        /**
         * 需要设置属性的名称
         *
         * @return
         */
        String localNeedSetPropertyName();

        /**
         * 类型
         *
         * @return
         */
        Class<?> localPropertyClassType();
    }

    /**
     * 判断list 是否空
     *
     * @param dataList
     * @return
     */
    public static boolean isEmpty(List dataList) {
        if (dataList == null || dataList.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 判断list 是否不为空
     *
     * @param dataList
     * @return
     */
    public static boolean isNotEmpty(List dataList) {
        if (dataList == null || dataList.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * list 取id 集合
     *
     * @param list
     * @param integerTransfer
     * @param <Source>
     * @return
     */
    public static <Source> List<Integer> getList(List<Source> list, IntegerTransfer<Source> integerTransfer) {
        return getTransferResultList(list, t -> integerTransfer.transfer(t));
    }

    /**
     * list 取id 集合
     *
     * @param list
     * @param stringTransfer
     * @param <Source>
     * @return
     */
    public static <Source> List<String> getList(List<Source> list, StringTransfer<Source> stringTransfer) {
        return getTransferResultList(list, t -> stringTransfer.transfer(t));
    }

    /**
     * list 取id 集合
     *
     * @param list
     * @param tTransfer
     * @param <Source>
     * @return
     */
    public static <Source, Result> List<Result> getTransferResultList(List<Source> list, Transfer<Source, Result> tTransfer) {
        if (isEmpty(list)) {
            //TODO source==null return
            return null;
        }
        List<Result> resultList = null;
        for (Source source : list) {
            if (source == null) {
                //TODO source==null continue
                continue;
            }
            try {
                Result result = tTransfer.transfer(source);
                if (result == null) {
                    //TODO result==null continue
                    continue;
                }
                if (resultList == null) resultList = new ArrayList<>();
                resultList.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }



    /**
     * 批量设置数据属性
     *
     * @param sources
     * @param dataList
     * @param objectKeyTransfer
     * @param <SourceData>
     * @param <TargetData>
     * @param <KeyData>
     * @return
     */
    public static <SourceData, TargetData, KeyData> List<SourceData> compareSetData(List<SourceData> sources, List<TargetData> dataList, ObjectKeyTransfer<KeyData, SourceData, TargetData> objectKeyTransfer) {
        return compareDealWithData(sources, dataList, objectKeyTransfer, (t, data) -> {
            try {
                BeanUtils.invokeSetData(t, data, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, null);
    }




    /**
     * 批量设置属性
     *
     * @param sources
     * @param dataList
     * @param sourceDataStringTransfer
     * @param targetDataStringTransfer
     * @param afterSetDataListener
     * @param <SourceData>
     * @param <TargetData>
     * @return
     */
    public static <SourceData, TargetData> List<SourceData> compareSetData(List<SourceData> sources, List<TargetData> dataList, ListUtil.StringTransfer<SourceData> sourceDataStringTransfer, ListUtil.StringTransfer<TargetData> targetDataStringTransfer, AfterSetDataListener<SourceData, TargetData> afterSetDataListener) {
        return compareDealWithData(sources, dataList, new ObjectKeyTransfer<String, SourceData, TargetData>() {

            @Override
            public String getSourceKey(SourceData f) {
                return sourceDataStringTransfer.transfer(f);
            }

            @Override
            public String getTargetKey(TargetData t) {
                return targetDataStringTransfer.transfer(t);
            }
        }, (t, data) -> {
            try {
                BeanUtils.invokeSetData(t, data, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, afterSetDataListener);
    }


    /**
     * 批量设置属性
     *
     * @param sources
     * @param dataList
     * @param objectKeyTransfer
     * @param afterSetDataListener
     * @param <SourceData>
     * @param <TargetData>
     * @param <KeyData>
     * @return
     */
    public static <SourceData, TargetData, KeyData> List<SourceData> compareSetData(List<SourceData> sources, List<TargetData> dataList, ObjectKeyTransfer<KeyData, SourceData, TargetData> objectKeyTransfer, AfterSetDataListener<SourceData, TargetData> afterSetDataListener) {
        return compareDealWithData(sources, dataList, objectKeyTransfer, (t, data) -> {
            try {
                BeanUtils.invokeSetData(t, data, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, afterSetDataListener);
    }


    /**
     * 使用接口，批量处理对象
     *
     * @param sources
     * @param dataList
     * @param compareDataSuccess
     * @param objectKeyTransfer
     * @param <SourceData>
     * @param <TargetData>
     * @param <KeyData>
     * @return
     */
    public static <SourceData, TargetData, KeyData> List<SourceData> compareDealWithData(List<SourceData> sources, List<TargetData> dataList, ObjectKeyTransfer<KeyData, SourceData, TargetData> objectKeyTransfer, CompareDataSuccess<SourceData, TargetData> compareDataSuccess) {
        return compareDealWithData(sources, dataList, objectKeyTransfer, compareDataSuccess, null);
    }


    public static <Source, DATA> List<Source> compareSetDataWithAnnotion(Class<Source> sourceClass, Class<DATA> dataClass, List<Source> sources, List<DATA> dataList) {
        return compareSetDataWithAnnotion(sourceClass, dataClass, sources, dataList, null);
    }

    /**
     * 使用注解 批量设置对象
     *
     * @param sources
     * @param dataList
     * @param afterSetDataListener
     * @param <Source>
     * @param <DATA>
     * @return
     */
    public static <Source, DATA> List<Source> compareSetDataWithAnnotion(Class<Source> sourceClass, Class<DATA> dataClass, List<Source> sources, List<DATA> dataList, AfterSetDataListener<Source, DATA> afterSetDataListener) {
        ListCompareSetData listCompareSetData = sourceClass.getAnnotation(ListCompareSetData.class);
        if (listCompareSetData == null || listCompareSetData.keys().length == 0) {
            throw new NullPointerException("对象未定义映射注解！=" + sourceClass.getName());
        }
        for (ListCompareSetDataItem listCompareSetDataItem : listCompareSetData.keys()) {
            //TODO check type
            if (!dataClass.getName().equals(listCompareSetDataItem.localPropertyClassType().getName())) {
                //TODO 类型不匹配 换下一个
                continue;
            }
            compareDealWithData(sources, dataList, new ObjectKeyTransfer<Object, Source, DATA>() {

                @Override
                public Object getTargetKey(DATA t) {
                    try {
                        Object object = BeanUtils.getProperty(t, listCompareSetDataItem.targetPropertyKey());
                        return object;
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                public Object getSourceKey(Source f) {
                    try {
                        Object object = BeanUtils.getProperty(f, listCompareSetDataItem.localPropertyKey());
                        return object;
                    } catch (Exception e) {
                        return null;
                    }
                }
            }, (t, data) -> {
                try {
                    BeanUtils.invokeSetData(t, data, listCompareSetDataItem.localNeedSetPropertyName());
                    if (afterSetDataListener != null) afterSetDataListener.setData(t, data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return sources;
    }


    public static <Source> void eachSetData(List<Source> sources, ExtendBeanBuildUtil.AfterSetDataListener<Source> afterSetDataListener) {
        eachSetData(sources, afterSetDataListener, null);
    }

    public static <Source> void eachSetData(List<Source> sources, ExtendBeanBuildUtil.AfterSetDataListener<Source> afterSetDataListener, EndForEach<List<Source>> endForEach) {
        if (sources != null) {
            for (Source source : sources) {
                try {
                    afterSetDataListener.setData(source);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (endForEach != null) {
                try {
                    endForEach.end(sources);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * @param sourceDatas          需要处理的数据
     * @param targetDatas          目标数据
     * @param objectKeyTransfer    从数据取key的接口
     * @param compareDataSuccess   对比成功的接口
     * @param afterSetDataListener 对比成功后的接口
     * @param <SourceData>
     * @param <TargetData>
     * @param <KeyData>            使用Map+List处理重复数据，遍历次数为加法。即最多为List(20)+List(20-重复数)+List(重复数)=40次。效率高于嵌套for循环20*20=400次，嵌套for循环耗时为它的2次方随数据量指数上升
     * @return
     */
    public static <SourceData, TargetData, KeyData> List<SourceData> compareDealWithData(List<SourceData> sourceDatas, List<TargetData> targetDatas, ObjectKeyTransfer<KeyData, SourceData, TargetData> objectKeyTransfer, CompareDataSuccess<SourceData, TargetData> compareDataSuccess, AfterSetDataListener<SourceData, TargetData> afterSetDataListener) {
        if (sourceDatas == null || targetDatas == null) return null;
        HashMap<KeyData, List<TargetData>> targetDataMap = new HashMap<>();
        for (TargetData targetData : targetDatas) {
            if (targetData == null) continue;
            KeyData key = null;
            try {
                key = objectKeyTransfer.getTargetKey(targetData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (key == null) continue;
            List<TargetData> targetDataNode = targetDataMap.get(key);
            if (targetDataNode == null) {
                targetDataNode = new ArrayList<>();
                targetDataNode.add(targetData);
                targetDataMap.put(key, targetDataNode);
            } else {
                targetDataNode.add(targetData);
            }
        }
        for (SourceData sourceData : sourceDatas) {
            if (sourceData == null) continue;
            KeyData key = objectKeyTransfer.getSourceKey(sourceData);
            if (key == null) continue;
            List<TargetData> targetDataNode = targetDataMap.get(key);
            if (targetDataNode == null || targetDataNode.size() == 0) continue;
            for (TargetData targetData : targetDataNode) {
                if (compareDataSuccess != null) try {
                    compareDataSuccess.compareDataSuccess(sourceData, targetData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (afterSetDataListener != null) try {
                    afterSetDataListener.setData(sourceData, targetData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        targetDataMap.clear();
        targetDataMap = null;
        return sourceDatas;
    }
}
