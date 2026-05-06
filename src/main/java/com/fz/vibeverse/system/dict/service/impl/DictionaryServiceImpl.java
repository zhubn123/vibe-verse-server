package com.fz.vibeverse.system.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.common.utils.OrderUtil;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.dict.domain.bo.DictItemSaveBo;
import com.fz.vibeverse.system.dict.domain.bo.DictTypeSaveBo;
import com.fz.vibeverse.system.dict.domain.entity.SysDictItem;
import com.fz.vibeverse.system.dict.domain.entity.SysDictType;
import com.fz.vibeverse.system.dict.domain.query.DictTypeQuery;
import com.fz.vibeverse.system.dict.domain.vo.DictItemVo;
import com.fz.vibeverse.system.dict.domain.vo.DictTypeVo;
import com.fz.vibeverse.system.dict.mapper.SysDictItemMapper;
import com.fz.vibeverse.system.dict.mapper.SysDictTypeMapper;
import com.fz.vibeverse.system.dict.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统字典服务实现。
 */
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private static final int STATUS_NORMAL = 0;

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;

    @Override
    public PageResult<DictTypeVo> queryTypePage(DictTypeQuery query) {
        DictTypeQuery normalized = query == null ? new DictTypeQuery() : query;
        IPage<SysDictType> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        String dictCode = normalizeOptional(normalized.getDictCode());
        if (StringUtils.isNotBlank(dictCode)) {
            wrapper.like(SysDictType::getDictCode, normalizeDictCode(dictCode));
        }

        String dictName = normalizeOptional(normalized.getDictName());
        if (StringUtils.isNotBlank(dictName)) {
            wrapper.like(SysDictType::getDictName, dictName);
        }

        String module = normalizeOptional(normalized.getModule());
        if (StringUtils.isNotBlank(module)) {
            wrapper.eq(SysDictType::getModule, module.toLowerCase(Locale.ROOT));
        }

        if (normalized.getStatus() != null) {
            wrapper.eq(SysDictType::getStatus, normalized.getStatus());
        }

        if (StringUtils.isBlank(normalized.getSortBy())) {
            wrapper.orderByAsc(SysDictType::getModule)
                    .orderByAsc(SysDictType::getDictCode);
        }

        IPage<SysDictType> result = dictTypeMapper.selectPage(page, wrapper);
        List<SysDictType> types = result.getRecords();
        Map<String, Long> itemCountMap = buildItemCountMap(types);
        List<DictTypeVo> records = types.stream()
                .map(type -> toTypeVo(type, itemCountMap))
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public DictTypeVo getTypeDetail(Integer id) {
        SysDictType type = requireTypeById(id);
        DictTypeVo vo = toTypeVo(type, buildItemCountMap(List.of(type)));
        vo.setItems(listItems(type.getDictCode(), true));
        return vo;
    }

    @Override
    public List<DictItemVo> listItems(String dictCode, boolean includeDisabled) {
        String normalizedCode = normalizeDictCode(dictCode);
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, normalizedCode)
                .orderByAsc(SysDictItem::getSortOrder)
                .orderByAsc(SysDictItem::getId);
        if (!includeDisabled) {
            wrapper.eq(SysDictItem::getStatus, STATUS_NORMAL);
        }
        return dictItemMapper.selectList(wrapper)
                .stream()
                .map(this::toItemVo)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createType(DictTypeSaveBo bo) {
        String dictCode = normalizeDictCode(bo == null ? null : bo.getDictCode());
        if (StringUtils.isBlank(dictCode)) {
            throw ApiException.badRequest("字典编码不能为空");
        }
        if (dictTypeMapper.selectByColumn(SysDictType::getDictCode, dictCode) != null) {
            throw ApiException.business("字典编码已存在");
        }

        SysDictType type = new SysDictType();
        applyType(type, bo, dictCode);
        dictTypeMapper.insert(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(Integer id, DictTypeSaveBo bo) {
        SysDictType existing = requireTypeById(id);
        String dictCode = normalizeDictCode(bo == null ? null : bo.getDictCode());
        if (!Objects.equals(existing.getDictCode(), dictCode)) {
            throw ApiException.badRequest("字典编码不允许修改");
        }

        SysDictType type = new SysDictType();
        type.setId(id);
        applyType(type, bo, dictCode);
        dictTypeMapper.updateById(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTypes(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的字典类型");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw ApiException.badRequest("字典类型ID不能为空");
        }
        List<SysDictType> types = dictTypeMapper.selectByIds(ids).stream()
                .filter(Objects::nonNull)
                .toList();
        if (types.size() != ids.stream().filter(Objects::nonNull).collect(Collectors.toSet()).size()) {
            throw ApiException.business("存在字典类型已被删除或不存在");
        }

        List<String> dictCodes = types.stream()
                .map(SysDictType::getDictCode)
                .filter(StringUtils::isNotBlank)
                .toList();
        if (!dictCodes.isEmpty()) {
            dictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>()
                    .in(SysDictItem::getDictCode, dictCodes));
        }
        dictTypeMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createItem(String dictCode, DictItemSaveBo bo) {
        String normalizedCode = normalizeDictCode(dictCode);
        requireTypeByCode(normalizedCode);
        String itemValue = normalizeRequired(bo == null ? null : bo.getItemValue(), "字典项值不能为空");
        ensureItemValueUnique(null, normalizedCode, itemValue);

        SysDictItem item = new SysDictItem();
        item.setDictCode(normalizedCode);
        applyItem(item, bo, itemValue);
        dictItemMapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Integer id, DictItemSaveBo bo) {
        SysDictItem existing = requireItemById(id);
        String itemValue = normalizeRequired(bo == null ? null : bo.getItemValue(), "字典项值不能为空");
        ensureItemValueUnique(id, existing.getDictCode(), itemValue);

        SysDictItem item = new SysDictItem();
        item.setId(id);
        item.setDictCode(existing.getDictCode());
        applyItem(item, bo, itemValue);
        dictItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItems(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的字典项");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw ApiException.badRequest("字典项ID不能为空");
        }
        List<SysDictItem> items = dictItemMapper.selectByIds(ids).stream()
                .filter(Objects::nonNull)
                .toList();
        if (items.size() != ids.stream().filter(Objects::nonNull).collect(Collectors.toSet()).size()) {
            throw ApiException.business("存在字典项已被删除或不存在");
        }
        dictItemMapper.deleteByIds(ids);
    }

    private void applyType(SysDictType type, DictTypeSaveBo bo, String dictCode) {
        type.setDictCode(dictCode);
        type.setDictName(normalizeRequired(bo == null ? null : bo.getDictName(), "字典名称不能为空"));
        type.setModule(normalizeRequired(bo == null ? null : bo.getModule(), "所属模块不能为空").toLowerCase(Locale.ROOT));
        type.setStatus(bo == null || bo.getStatus() == null ? STATUS_NORMAL : bo.getStatus());
        type.setRemark(StringUtils.defaultString(normalizeOptional(bo == null ? null : bo.getRemark())));
    }

    private void applyItem(SysDictItem item, DictItemSaveBo bo, String itemValue) {
        item.setItemValue(itemValue);
        item.setItemLabel(normalizeRequired(bo == null ? null : bo.getItemLabel(), "字典项标签不能为空"));
        item.setSortOrder(bo == null || bo.getSortOrder() == null ? 0 : bo.getSortOrder());
        item.setStatus(bo == null || bo.getStatus() == null ? STATUS_NORMAL : bo.getStatus());
        item.setRemark(StringUtils.defaultString(normalizeOptional(bo == null ? null : bo.getRemark())));
    }

    private SysDictType requireTypeById(Integer id) {
        if (id == null) {
            throw ApiException.badRequest("字典类型ID不能为空");
        }
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw ApiException.business("字典类型不存在");
        }
        return type;
    }

    private SysDictType requireTypeByCode(String dictCode) {
        if (StringUtils.isBlank(dictCode)) {
            throw ApiException.badRequest("字典编码不能为空");
        }
        SysDictType type = dictTypeMapper.selectByColumn(SysDictType::getDictCode, dictCode);
        if (type == null) {
            throw ApiException.business("字典类型不存在");
        }
        return type;
    }

    private SysDictItem requireItemById(Integer id) {
        if (id == null) {
            throw ApiException.badRequest("字典项ID不能为空");
        }
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw ApiException.business("字典项不存在");
        }
        return item;
    }

    private void ensureItemValueUnique(Integer itemId, String dictCode, String itemValue) {
        SysDictItem duplicate = dictItemMapper.selectOne(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, dictCode)
                .eq(SysDictItem::getItemValue, itemValue));
        if (duplicate != null && !Objects.equals(duplicate.getId(), itemId)) {
            throw ApiException.business("字典项值已存在");
        }
    }

    private Map<String, Long> buildItemCountMap(List<SysDictType> types) {
        if (types == null || types.isEmpty()) {
            return Map.of();
        }
        Set<String> dictCodes = types.stream()
                .map(SysDictType::getDictCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (dictCodes.isEmpty()) {
            return Map.of();
        }
        return dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                        .in(SysDictItem::getDictCode, dictCodes))
                .stream()
                .collect(Collectors.groupingBy(SysDictItem::getDictCode, LinkedHashMap::new, Collectors.counting()));
    }

    private String normalizeDictCode(String dictCode) {
        return StringUtils.trimToEmpty(dictCode).toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String input, String message) {
        String normalized = normalizeOptional(input);
        if (StringUtils.isBlank(normalized)) {
            throw ApiException.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }

    private DictTypeVo toTypeVo(SysDictType type, Map<String, Long> itemCountMap) {
        DictTypeVo vo = new DictTypeVo();
        vo.setId(type.getId());
        vo.setDictCode(type.getDictCode());
        vo.setDictName(type.getDictName());
        vo.setModule(type.getModule());
        vo.setStatus(type.getStatus());
        vo.setRemark(type.getRemark());
        vo.setItemCount(itemCountMap.getOrDefault(type.getDictCode(), 0L));
        return vo;
    }

    private DictItemVo toItemVo(SysDictItem item) {
        DictItemVo vo = new DictItemVo();
        vo.setId(item.getId());
        vo.setDictCode(item.getDictCode());
        vo.setValue(item.getItemValue());
        vo.setLabel(item.getItemLabel());
        vo.setSortOrder(item.getSortOrder());
        vo.setStatus(item.getStatus());
        vo.setRemark(item.getRemark());
        return vo;
    }
}
