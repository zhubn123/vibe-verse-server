package com.fz.vibeverse.system.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.common.utils.OrderUtil;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.config.domain.bo.SystemConfigSaveBo;
import com.fz.vibeverse.system.config.domain.entity.SysConfig;
import com.fz.vibeverse.system.config.domain.query.SystemConfigQuery;
import com.fz.vibeverse.system.config.domain.vo.SystemConfigVo;
import com.fz.vibeverse.system.config.mapper.SysConfigMapper;
import com.fz.vibeverse.system.config.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 系统参数配置服务实现。
 */
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final int STATUS_NORMAL = 1;

    private final SysConfigMapper sysConfigMapper;

    @Override
    public PageResult<SystemConfigVo> queryConfigPage(SystemConfigQuery query) {
        SystemConfigQuery normalized = query == null ? new SystemConfigQuery() : query;
        IPage<SysConfig> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        String configKey = normalizeOptional(normalized.getConfigKey());
        if (StringUtils.isNotBlank(configKey)) {
            wrapper.like(SysConfig::getConfigKey, normalizeConfigKey(configKey));
        }

        String configName = normalizeOptional(normalized.getConfigName());
        if (StringUtils.isNotBlank(configName)) {
            wrapper.like(SysConfig::getConfigName, configName);
        }

        if (normalized.getStatus() != null) {
            wrapper.eq(SysConfig::getStatus, normalized.getStatus());
        }

        if (StringUtils.isBlank(normalized.getSortBy())) {
            wrapper.orderByAsc(SysConfig::getConfigKey);
        }

        IPage<SysConfig> result = sysConfigMapper.selectPage(page, wrapper);
        List<SystemConfigVo> records = result.getRecords()
                .stream()
                .map(this::toVo)
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public SystemConfigVo getConfigDetail(Long id) {
        return toVo(requireConfigById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createConfig(SystemConfigSaveBo bo) {
        String configKey = normalizeConfigKey(bo == null ? null : bo.getConfigKey());
        if (StringUtils.isBlank(configKey)) {
            throw ApiException.badRequest("配置键不能为空");
        }
        if (sysConfigMapper.selectByColumn(SysConfig::getConfigKey, configKey) != null) {
            throw ApiException.business("配置键已存在");
        }

        SysConfig config = new SysConfig();
        applyConfig(config, bo, configKey);
        sysConfigMapper.insert(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long id, SystemConfigSaveBo bo) {
        SysConfig existing = requireConfigById(id);
        String configKey = normalizeConfigKey(bo == null ? null : bo.getConfigKey());
        if (!Objects.equals(existing.getConfigKey(), configKey)) {
            throw ApiException.badRequest("配置键不允许修改");
        }

        SysConfig config = new SysConfig();
        config.setId(id);
        applyConfig(config, bo, configKey);
        sysConfigMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的配置");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw ApiException.badRequest("配置ID不能为空");
        }

        List<SysConfig> configs = sysConfigMapper.selectByIds(ids).stream()
                .filter(Objects::nonNull)
                .toList();
        if (configs.size() != new LinkedHashSet<>(ids).size()) {
            throw ApiException.business("存在配置已被删除或不存在");
        }
        sysConfigMapper.deleteByIds(ids);
    }

    private void applyConfig(SysConfig config, SystemConfigSaveBo bo, String configKey) {
        config.setConfigKey(configKey);
        config.setConfigName(normalizeRequired(bo == null ? null : bo.getConfigName(), "配置名称不能为空"));
        config.setConfigValue(StringUtils.defaultString(normalizeOptional(bo == null ? null : bo.getConfigValue())));
        config.setValueType(normalizeRequired(bo == null ? null : bo.getValueType(), "值类型不能为空").toLowerCase(Locale.ROOT));
        config.setStatus(bo == null || bo.getStatus() == null ? STATUS_NORMAL : bo.getStatus());
        config.setRemark(StringUtils.defaultString(normalizeOptional(bo == null ? null : bo.getRemark())));
    }

    private SysConfig requireConfigById(Long id) {
        if (id == null) {
            throw ApiException.badRequest("配置ID不能为空");
        }
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw ApiException.business("配置不存在");
        }
        return config;
    }

    private String normalizeConfigKey(String configKey) {
        return StringUtils.trimToEmpty(configKey).toLowerCase(Locale.ROOT);
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

    private SystemConfigVo toVo(SysConfig config) {
        SystemConfigVo vo = new SystemConfigVo();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigName(config.getConfigName());
        vo.setConfigValue(config.getConfigValue());
        vo.setValueType(config.getValueType());
        vo.setStatus(config.getStatus());
        vo.setRemark(config.getRemark());
        return vo;
    }
}
