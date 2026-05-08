package com.fz.vibeverse.system.audit.annotation;

import com.fz.vibeverse.system.audit.model.AuditType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 业务模块，例如 ROLE / DICT / CONFIG。
     */
    String module();

    /**
     * 操作类型。
     */
    AuditType type() default AuditType.OTHER;

    /**
     * 操作描述。
     */
    String description() default "";

    /**
     * 是否记录请求参数摘要。
     */
    boolean logParams() default true;

    /**
     * 是否记录返回结果摘要。
     */
    boolean logResult() default false;

    /**
     * 额外排除的参数名。
     */
    String[] excludeParamNames() default {};

    /**
     * 摘要截断长度。
     */
    int truncateLength() default 255;
}
