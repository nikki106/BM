package com.myproject.bm.controller;

import com.myproject.bm.common.Result;
import com.myproject.bm.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(statsService.stats());
    }
}
