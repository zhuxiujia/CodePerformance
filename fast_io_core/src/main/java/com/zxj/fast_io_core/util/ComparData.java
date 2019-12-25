package com.zxj.fast_io_core.util;

import java.util.List;

/**
 * @author zhuxiujie
 * @since 2019/12/25
 */

public class ComparData<SourceData, TargetData> {
    List<SourceData> sources;
    List<TargetData> targets;
    ListUtil.StringTransfer<SourceData> sourceDataStringTransfer;
    ListUtil.StringTransfer<TargetData> targetDataStringTransfer;

    public  ComparData<SourceData, TargetData> from(List<SourceData> sources, List<TargetData> targets){
        this.sources=sources;
        this.targets=targets;
        return this;
    }

    public ComparData<SourceData, TargetData> key(ListUtil.StringTransfer<SourceData> sourceDataStringTransfer, ListUtil.StringTransfer<TargetData> targetDataStringTransfer){
        this.sourceDataStringTransfer=sourceDataStringTransfer;
        this.targetDataStringTransfer=targetDataStringTransfer;
        return this;
    }

    public void complete(ListUtil.AfterSetDataListener<SourceData, TargetData> afterSetDataListener){
        ListUtil.compareSetData(this.sources,this.targets,this.sourceDataStringTransfer,this.targetDataStringTransfer,afterSetDataListener);
    }

}
