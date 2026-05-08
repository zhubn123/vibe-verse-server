package com.fz.vibeverse.system.oss.storage;

import org.springframework.core.io.Resource;

/**
 * 对象读取结果。
 */
public record ObjectStorageResource(Resource resource, long size) {
}
