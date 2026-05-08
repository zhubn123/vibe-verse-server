package com.fz.vibeverse.system.oss.storage.local;

import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.oss.config.OssProperties;
import com.fz.vibeverse.system.oss.storage.ObjectStorage;
import com.fz.vibeverse.system.oss.storage.ObjectStorageResource;
import com.fz.vibeverse.system.oss.storage.StoreObjectRequest;
import com.fz.vibeverse.system.oss.storage.StoredObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地磁盘对象存储实现。
 */
@Service
public class LocalObjectStorage implements ObjectStorage {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final OssProperties ossProperties;

    public LocalObjectStorage(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public StoredObject store(StoreObjectRequest request) {
        if (request == null || request.inputStream() == null) {
            throw ApiException.badRequest("上传文件不能为空");
        }

        Path target = null;
        try {
            Path root = resolveRoot();
            String objectKey = buildObjectKey(request.bucket(), request.extension());
            target = resolveWithinRoot(root, objectKey);
            Files.createDirectories(target.getParent());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream inputStream = new DigestInputStream(request.inputStream(), digest)) {
                Files.copy(inputStream, target);
            }

            long size = Files.size(target);
            String checksum = HexFormat.of().formatHex(digest.digest());
            return new StoredObject(objectKey, objectKey, size, checksum, storageType());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            deletePartiallyWrittenFile(target);
            throw ApiException.business("文件写入失败：" + ex.getMessage());
        }
    }

    @Override
    public ObjectStorageResource load(String storagePath) {
        Path root = resolveRoot();
        Path target = resolveWithinRoot(root, storagePath);
        if (!Files.isRegularFile(target)) {
            throw ApiException.business("文件实体不存在");
        }
        try {
            return new ObjectStorageResource(new FileSystemResource(target), Files.size(target));
        } catch (IOException ex) {
            throw ApiException.business("文件读取失败：" + ex.getMessage());
        }
    }

    @Override
    public void delete(String storagePath) {
        Path root = resolveRoot();
        Path target = resolveWithinRoot(root, storagePath);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw ApiException.business("文件删除失败：" + ex.getMessage());
        }
    }

    @Override
    public String storageType() {
        return OssProperties.StorageType.LOCAL.name();
    }

    private Path resolveRoot() {
        String rootPath = ossProperties.getLocal() == null ? null : ossProperties.getLocal().getRootPath();
        if (StringUtils.isBlank(rootPath)) {
            throw ApiException.business("对象存储根目录未配置");
        }
        return Path.of(rootPath).toAbsolutePath().normalize();
    }

    private Path resolveWithinRoot(Path root, String storagePath) {
        if (StringUtils.isBlank(storagePath)) {
            throw ApiException.badRequest("存储路径不能为空");
        }
        Path target = root.resolve(storagePath).normalize();
        if (!target.startsWith(root)) {
            throw ApiException.badRequest("存储路径非法");
        }
        return target;
    }

    private String buildObjectKey(String bucket, String extension) {
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        String suffix = normalizeExtension(extension);
        return bucket + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    private String normalizeExtension(String extension) {
        String normalized = StringUtils.trimToEmpty(extension)
                .replace(".", "")
                .toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(normalized)) {
            return "";
        }
        return "." + normalized;
    }

    private void deletePartiallyWrittenFile(Path target) {
        if (target == null) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // The original write failure is more useful to the caller.
        }
    }
}
