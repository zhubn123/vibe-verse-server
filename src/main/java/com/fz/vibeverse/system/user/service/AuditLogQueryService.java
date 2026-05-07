package com.fz.vibeverse.system.user.service;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.system.user.domain.query.AuditLogQuery;
import com.fz.vibeverse.system.user.domain.vo.AuditLogVo;

/**
 * 审计日志查询服务。
 */
public interface AuditLogQueryService {

    /**
     * 分页查询审计日志。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<AuditLogVo> queryAuditLogPage(AuditLogQuery query);
}
