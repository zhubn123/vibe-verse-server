package com.fz.vibeverse.system.user.mapper;

import com.fz.vibeverse.common.BaseMapperPlus;
import com.fz.vibeverse.system.user.domain.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 安全审计日志 Mapper。
 */
@Mapper
public interface SysAuditLogMapper extends BaseMapperPlus<SysAuditLog> {
}
