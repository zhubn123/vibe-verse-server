package com.fz.vibeverse.system.oss.storage;

import java.io.InputStream;

/**
 * 对象写入请求。
 */
public record StoreObjectRequest(
        String bucket,
        String originalFilename,
        String extension,
        String contentType,
        InputStream inputStream
) {
}
