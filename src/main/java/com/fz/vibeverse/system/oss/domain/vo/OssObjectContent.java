package com.fz.vibeverse.system.oss.domain.vo;

import org.springframework.core.io.Resource;

/**
 * 对象二进制读取结果。
 */
public record OssObjectContent(
        Long id,
        String originalName,
        String contentType,
        long size,
        Resource resource
) {
}
