package com.fz.vibeverse.startup;

import com.fz.vibeverse.exception.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查接口。
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, String>> health() {
        return Result.success(Map.of("status", "UP"));
    }
}
