package com.fz.vibeverse.system.oss.service;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.system.oss.domain.query.OssObjectQuery;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectContent;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 对象存储服务。
 */
public interface OssObjectService {

    PageResult<OssObjectVo> queryObjectPage(OssObjectQuery query);

    OssObjectVo getObjectDetail(Long id);

    OssObjectVo uploadObject(String bucket, MultipartFile file, String remark);

    OssObjectContent loadObjectContent(Long id);

    void deleteObjects(List<Long> ids);
}
