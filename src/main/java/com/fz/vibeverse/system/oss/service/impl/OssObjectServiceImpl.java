package com.fz.vibeverse.system.oss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.common.utils.OrderUtil;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.oss.config.OssProperties;
import com.fz.vibeverse.system.oss.domain.entity.OssObject;
import com.fz.vibeverse.system.oss.domain.query.OssObjectQuery;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectContent;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectVo;
import com.fz.vibeverse.system.oss.mapper.OssObjectMapper;
import com.fz.vibeverse.system.oss.service.OssObjectService;
import com.fz.vibeverse.system.oss.storage.ObjectStorage;
import com.fz.vibeverse.system.oss.storage.ObjectStorageResource;
import com.fz.vibeverse.system.oss.storage.StoreObjectRequest;
import com.fz.vibeverse.system.oss.storage.StoredObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 对象存储服务实现。
 */
@Service
@RequiredArgsConstructor
public class OssObjectServiceImpl implements OssObjectService {

    private static final int STATUS_NORMAL = 1;
    private static final String ACCESS_POLICY_PRIVATE = "private";
    private static final int MAX_BUCKET_LENGTH = 64;
    private static final int MAX_ORIGINAL_NAME_LENGTH = 255;
    private static final int MAX_CONTENT_TYPE_LENGTH = 128;
    private static final int MAX_EXTENSION_LENGTH = 32;
    private static final int MAX_REMARK_LENGTH = 255;

    private final OssObjectMapper ossObjectMapper;
    private final ObjectStorage objectStorage;
    private final OssProperties ossProperties;

    @Override
    public PageResult<OssObjectVo> queryObjectPage(OssObjectQuery query) {
        OssObjectQuery normalized = query == null ? new OssObjectQuery() : query;
        IPage<OssObject> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<OssObject> wrapper = new LambdaQueryWrapper<>();
        String bucket = normalizeOptional(normalized.getBucket());
        if (StringUtils.isNotBlank(bucket)) {
            wrapper.eq(OssObject::getBucket, bucket.toLowerCase(Locale.ROOT));
        }

        String originalName = normalizeOptional(normalized.getOriginalName());
        if (StringUtils.isNotBlank(originalName)) {
            wrapper.like(OssObject::getOriginalName, originalName);
        }

        String contentType = normalizeOptional(normalized.getContentType());
        if (StringUtils.isNotBlank(contentType)) {
            wrapper.like(OssObject::getContentType, contentType);
        }

        if (normalized.getStatus() != null) {
            wrapper.eq(OssObject::getStatus, normalized.getStatus());
        }

        if (StringUtils.isBlank(normalized.getSortBy())) {
            wrapper.orderByDesc(OssObject::getCreateTime);
        }

        IPage<OssObject> result = ossObjectMapper.selectPage(page, wrapper);
        List<OssObjectVo> records = result.getRecords()
                .stream()
                .map(this::toVo)
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public OssObjectVo getObjectDetail(Long id) {
        return toVo(requireObjectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OssObjectVo uploadObject(String bucket, MultipartFile file, String remark) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("上传文件不能为空");
        }
        long maxFileSize = ossProperties.getMaxFileSize() == null ? 0 : ossProperties.getMaxFileSize().toBytes();
        if (maxFileSize > 0 && file.getSize() > maxFileSize) {
            throw ApiException.badRequest("文件大小不能超过 " + ossProperties.getMaxFileSize());
        }

        try {
            return saveObject(bucket, file.getOriginalFilename(), file.getContentType(), file.getInputStream(), remark);
        } catch (IOException ex) {
            throw ApiException.business("读取上传文件失败：" + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OssObjectVo saveObject(String bucket, String originalName, String contentType, InputStream inputStream, String remark) {
        if (inputStream == null) {
            throw ApiException.badRequest("文件内容不能为空");
        }

        String normalizedBucket = normalizeBucket(bucket);
        String normalizedOriginalName = normalizeOriginalName(originalName);
        String extension = resolveExtension(normalizedOriginalName);
        String normalizedContentType = truncate(normalizeOptional(contentType), MAX_CONTENT_TYPE_LENGTH);
        String normalizedRemark = truncate(StringUtils.defaultString(normalizeOptional(remark)), MAX_REMARK_LENGTH);

        StoredObject storedObject = null;
        try {
            storedObject = objectStorage.store(new StoreObjectRequest(
                    normalizedBucket,
                    normalizedOriginalName,
                    extension,
                    normalizedContentType,
                    inputStream
            ));

            OssObject object = new OssObject();
            object.setBucket(normalizedBucket);
            object.setObjectKey(storedObject.objectKey());
            object.setOriginalName(normalizedOriginalName);
            object.setExtension(extension);
            object.setContentType(normalizedContentType);
            object.setSize(storedObject.size());
            object.setChecksumSha256(storedObject.checksumSha256());
            object.setStorageType(storedObject.storageType());
            object.setStoragePath(storedObject.storagePath());
            object.setAccessPolicy(ACCESS_POLICY_PRIVATE);
            object.setStatus(STATUS_NORMAL);
            object.setRemark(normalizedRemark);
            ossObjectMapper.insert(object);
            return toVo(object);
        } catch (RuntimeException ex) {
            if (storedObject != null) {
                objectStorage.delete(storedObject.storagePath());
            }
            throw ex;
        }
    }

    @Override
    public OssObjectContent loadObjectContent(Long id) {
        OssObject object = requireObjectById(id);
        if (!Objects.equals(object.getStatus(), STATUS_NORMAL)) {
            throw ApiException.business("文件已停用");
        }
        ObjectStorageResource resource = objectStorage.load(object.getStoragePath());
        long size = object.getSize() == null ? resource.size() : object.getSize();
        return new OssObjectContent(object.getId(), object.getOriginalName(), object.getContentType(), size, resource.resource());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteObjects(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的文件");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw ApiException.badRequest("文件ID不能为空");
        }

        LinkedHashSet<Long> uniqueIds = ids.stream().collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        List<OssObject> objects = ossObjectMapper.selectByIds(uniqueIds).stream()
                .filter(Objects::nonNull)
                .toList();
        if (objects.size() != uniqueIds.size()) {
            throw ApiException.business("存在文件已被删除或不存在");
        }

        for (OssObject object : objects) {
            objectStorage.delete(object.getStoragePath());
        }
        ossObjectMapper.deleteByIds(uniqueIds);
    }

    private OssObject requireObjectById(Long id) {
        if (id == null) {
            throw ApiException.badRequest("文件ID不能为空");
        }
        OssObject object = ossObjectMapper.selectById(id);
        if (object == null) {
            throw ApiException.business("文件不存在");
        }
        return object;
    }

    private String normalizeBucket(String bucket) {
        String fallbackBucket = StringUtils.defaultIfBlank(ossProperties.getDefaultBucket(), "attachment");
        String normalized = StringUtils.defaultIfBlank(normalizeOptional(bucket), fallbackBucket)
                .toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(normalized)) {
            throw ApiException.badRequest("bucket 不能为空");
        }
        if (normalized.length() > MAX_BUCKET_LENGTH || !normalized.matches("^[a-z0-9][a-z0-9_-]{0,63}$")) {
            throw ApiException.badRequest("bucket 格式非法");
        }
        return normalized;
    }

    private String normalizeOriginalName(String originalName) {
        String normalized = normalizeOptional(originalName);
        if (StringUtils.isBlank(normalized)) {
            return "unnamed";
        }
        normalized = normalized.replace("\\", "/");
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }
        normalized = truncate(normalized, MAX_ORIGINAL_NAME_LENGTH);
        return StringUtils.defaultIfBlank(normalized, "unnamed");
    }

    private String resolveExtension(String originalName) {
        if (StringUtils.isBlank(originalName)) {
            return "";
        }
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalName.length() - 1) {
            return "";
        }
        String extension = originalName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return truncate(extension.replaceAll("[^a-z0-9_-]", ""), MAX_EXTENSION_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }

    private OssObjectVo toVo(OssObject object) {
        OssObjectVo vo = new OssObjectVo();
        vo.setId(object.getId());
        vo.setBucket(object.getBucket());
        vo.setObjectKey(object.getObjectKey());
        vo.setOriginalName(object.getOriginalName());
        vo.setExtension(object.getExtension());
        vo.setContentType(object.getContentType());
        vo.setSize(object.getSize());
        vo.setChecksumSha256(object.getChecksumSha256());
        vo.setStorageType(object.getStorageType());
        vo.setAccessPolicy(object.getAccessPolicy());
        vo.setStatus(object.getStatus());
        vo.setRemark(object.getRemark());
        vo.setCreateTime(object.getCreateTime());
        vo.setUpdateTime(object.getUpdateTime());
        if (object.getId() != null) {
            vo.setDownloadUrl("/api/oss-objects/" + object.getId() + "/download");
            vo.setPreviewUrl("/api/oss-objects/" + object.getId() + "/preview");
        }
        return vo;
    }
}
