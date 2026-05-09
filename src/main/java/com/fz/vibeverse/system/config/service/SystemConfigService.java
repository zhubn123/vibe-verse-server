package com.fz.vibeverse.system.config.service;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.system.config.domain.bo.SystemConfigSaveBo;
import com.fz.vibeverse.system.config.domain.query.SystemConfigQuery;
import com.fz.vibeverse.system.config.domain.vo.AppConfigVo;
import com.fz.vibeverse.system.config.domain.vo.SystemConfigVo;

import java.util.List;

/**
 * 系统参数配置服务。
 */
public interface SystemConfigService {

    /**
     * 查询前端应用展示配置。
     *
     * @return 应用展示配置
     */
    AppConfigVo getAppConfig();

    /**
     * 分页查询系统参数配置。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<SystemConfigVo> queryConfigPage(SystemConfigQuery query);

    /**
     * 查询系统参数配置详情。
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    SystemConfigVo getConfigDetail(Long id);

    /**
     * 创建系统参数配置。
     *
     * @param bo 保存参数
     */
    void createConfig(SystemConfigSaveBo bo);

    /**
     * 更新系统参数配置。
     *
     * @param id 配置 ID
     * @param bo 保存参数
     */
    void updateConfig(Long id, SystemConfigSaveBo bo);

    /**
     * 批量删除系统参数配置。
     *
     * @param ids 配置 ID 集合
     */
    void deleteConfigs(List<Long> ids);
}
