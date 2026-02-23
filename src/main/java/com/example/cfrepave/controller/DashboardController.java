package com.example.cfrepave.controller;

import com.example.cfrepave.model.InstanceInfo;
import com.example.cfrepave.service.CfInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final CfInfoService cfInfoService;

    public DashboardController(CfInfoService cfInfoService) {
        this.cfInfoService = cfInfoService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/api/instance")
    @ResponseBody
    public InstanceInfo getInstance() {
        return cfInfoService.getInstanceInfo();
    }

    @GetMapping("/api/services")
    @ResponseBody
    public List<Map<String, Object>> getServices() {
        return cfInfoService.getBoundServices();
    }

    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
