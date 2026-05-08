package com.fz.vibeverse.system.oss.storage;

/**
 * 对象二进制存储接口。
 */
public interface ObjectStorage {

    StoredObject store(StoreObjectRequest request);

    ObjectStorageResource load(String storagePath);

    void delete(String storagePath);

    String storageType();
}
