package com.fz.vibeverse.system.oss.storage;

/**
 * 已写入对象信息。
 */
public record StoredObject(
        String objectKey,
        String storagePath,
        long size,
        String checksumSha256,
        String storageType
) {
}
