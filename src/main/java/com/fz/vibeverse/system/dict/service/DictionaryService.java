package com.fz.vibeverse.system.dict.service;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.system.dict.domain.bo.DictItemSaveBo;
import com.fz.vibeverse.system.dict.domain.bo.DictTypeSaveBo;
import com.fz.vibeverse.system.dict.domain.query.DictTypeQuery;
import com.fz.vibeverse.system.dict.domain.vo.DictItemVo;
import com.fz.vibeverse.system.dict.domain.vo.DictTypeVo;

import java.util.List;

/**
 * 系统字典服务。
 */
public interface DictionaryService {

    /**
     * 分页查询字典类型。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<DictTypeVo> queryTypePage(DictTypeQuery query);

    /**
     * 查询字典类型详情。
     *
     * @param id 字典类型 ID
     * @return 字典类型
     */
    DictTypeVo getTypeDetail(Integer id);

    /**
     * 查询启用字典项。
     *
     * @param dictCode 字典编码
     * @param includeDisabled 是否包含停用项
     * @return 字典项列表
     */
    List<DictItemVo> listItems(String dictCode, boolean includeDisabled);

    /**
     * 创建字典类型。
     *
     * @param bo 保存参数
     */
    void createType(DictTypeSaveBo bo);

    /**
     * 更新字典类型。
     *
     * @param id 字典类型 ID
     * @param bo 保存参数
     */
    void updateType(Integer id, DictTypeSaveBo bo);

    /**
     * 删除字典类型。
     *
     * @param ids 字典类型 ID 集合
     */
    void deleteTypes(List<Integer> ids);

    /**
     * 创建字典项。
     *
     * @param dictCode 字典编码
     * @param bo 保存参数
     */
    void createItem(String dictCode, DictItemSaveBo bo);

    /**
     * 更新字典项。
     *
     * @param id 字典项 ID
     * @param bo 保存参数
     */
    void updateItem(Integer id, DictItemSaveBo bo);

    /**
     * 删除字典项。
     *
     * @param ids 字典项 ID 集合
     */
    void deleteItems(List<Integer> ids);
}
