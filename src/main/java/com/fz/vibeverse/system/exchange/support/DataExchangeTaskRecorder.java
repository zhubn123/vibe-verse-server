package com.fz.vibeverse.system.exchange.support;

import com.fz.vibeverse.system.exchange.domain.entity.DataExchangeTask;
import com.fz.vibeverse.system.exchange.domain.vo.DataExchangeTaskVo;
import com.fz.vibeverse.system.exchange.mapper.DataExchangeTaskMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 导入导出任务留痕。
 */
@Component
@RequiredArgsConstructor
public class DataExchangeTaskRecorder {

    private static final int MAX_MESSAGE_LENGTH = 512;

    private final DataExchangeTaskMapper dataExchangeTaskMapper;

    public DataExchangeTask startImport(String scene, Long sourceObjectId, String remark, String message) {
        DataExchangeTask task = new DataExchangeTask();
        task.setDirection(DataExchangeConstants.DIRECTION_IMPORT);
        task.setScene(scene);
        task.setStatus(DataExchangeConstants.STATUS_RUNNING);
        task.setSourceObjectId(sourceObjectId);
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setMessage(StringUtils.defaultString(message));
        task.setRemark(StringUtils.defaultString(normalizeOptional(remark)));
        task.setStartTime(LocalDateTime.now());
        dataExchangeTaskMapper.insert(task);
        return task;
    }

    public DataExchangeTaskVo finishImport(Long taskId, DataImportSummary summary, Long errorObjectId,
                                           String successMessage, String failedMessage) {
        DataExchangeTask update = new DataExchangeTask();
        update.setId(taskId);
        update.setStatus(summary.failCount() > 0
                ? DataExchangeConstants.STATUS_FAILED
                : DataExchangeConstants.STATUS_SUCCESS);
        update.setTotalCount(summary.totalCount());
        update.setSuccessCount(summary.successCount());
        update.setFailCount(summary.failCount());
        update.setErrorObjectId(errorObjectId);
        update.setMessage(summary.failCount() > 0 ? failedMessage : successMessage);
        update.setFinishTime(LocalDateTime.now());
        dataExchangeTaskMapper.updateById(update);
        return toVo(dataExchangeTaskMapper.selectById(taskId));
    }

    public void recordSuccessfulExport(String scene, Long resultObjectId, int totalCount, String remark, String message) {
        DataExchangeTask task = new DataExchangeTask();
        task.setDirection(DataExchangeConstants.DIRECTION_EXPORT);
        task.setScene(scene);
        task.setStatus(DataExchangeConstants.STATUS_SUCCESS);
        task.setResultObjectId(resultObjectId);
        task.setTotalCount(totalCount);
        task.setSuccessCount(totalCount);
        task.setFailCount(0);
        task.setMessage(StringUtils.defaultString(message));
        task.setRemark(StringUtils.defaultString(normalizeOptional(remark)));
        task.setStartTime(LocalDateTime.now());
        task.setFinishTime(LocalDateTime.now());
        dataExchangeTaskMapper.insert(task);
    }

    public DataExchangeTaskVo toVo(DataExchangeTask task) {
        DataExchangeTaskVo vo = new DataExchangeTaskVo();
        vo.setId(task.getId());
        vo.setDirection(task.getDirection());
        vo.setScene(task.getScene());
        vo.setStatus(task.getStatus());
        vo.setSourceObjectId(task.getSourceObjectId());
        vo.setResultObjectId(task.getResultObjectId());
        vo.setErrorObjectId(task.getErrorObjectId());
        vo.setTotalCount(task.getTotalCount());
        vo.setSuccessCount(task.getSuccessCount());
        vo.setFailCount(task.getFailCount());
        vo.setMessage(truncate(task.getMessage(), MAX_MESSAGE_LENGTH));
        vo.setRemark(task.getRemark());
        vo.setStartTime(task.getStartTime());
        vo.setFinishTime(task.getFinishTime());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());
        vo.setSourceDownloadUrl(buildDownloadUrl(task.getSourceObjectId()));
        vo.setResultDownloadUrl(buildDownloadUrl(task.getResultObjectId()));
        vo.setErrorDownloadUrl(buildDownloadUrl(task.getErrorObjectId()));
        return vo;
    }

    private String buildDownloadUrl(Long objectId) {
        return objectId == null ? null : "/api/oss-objects/" + objectId + "/download";
    }

    private String truncate(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }
}
