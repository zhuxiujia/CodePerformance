package com.zxj.fast_io_core.util;

/**
 * @author zhuxiujie
 * @since 2019/12/25
 */

public class StringObjectKeyTransfer implements ListUtil.ObjectKeyTransfer<String, Object, Object> {

    String source;
    String target;

    public StringObjectKeyTransfer(String source_key,String target_key){
        this.source=source_key;
        this.target=target_key;
    }

    @Override
    public String getSourceKey(Object f) {
        return source;
    }

    @Override
    public String getTargetKey(Object t) {
        return target;
    }
}
