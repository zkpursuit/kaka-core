package com.kaka.numerical;

/**
 * 数据单元分析匹配
 *
 * @param <V> 泛型参数
 * @author zhoukai
 */
public interface IAnalyzer<V> {

    /**
     * 设置源数据
     *
     * @param sourceData 源数据
     */
    void setSourceData(V sourceData);

    /**
     * 根据数据单元名查找数据单元内容
     *
     * @param title 数据单元名
     * @return 数据单元内容
     */
    String getContent(String title);

}
