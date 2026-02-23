package com.example.cfrepave.service;

import com.example.cfrepave.model.InstanceInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CfInfoService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Instant startTime = Instant.now();

    public InstanceInfo getInstanceInfo() {
        InstanceInfo info = new InstanceInfo();

        info.setInstanceIndex(env("CF_INSTANCE_INDEX", "0"));
        info.setInstanceId(env("CF_INSTANCE_GUID", "local-dev-" + ProcessHandle.current().pid()));
        info.setInstanceIp(env("CF_INSTANCE_IP", "127.0.0.1"));
        info.setInstancePort(env("CF_INSTANCE_PORT", "8080"));
        info.setUptimeSeconds(Instant.now().getEpochSecond() - startTime.getEpochSecond());

        Map<String, Object> vcapApp = parseVcapApplication();
        info.setAppName((String) vcapApp.getOrDefault("application_name", "cf-repave-stats (local)"));
        info.setAppId((String) vcapApp.getOrDefault("application_id", "local-dev"));

        info.setTimestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC)));

        // Resource limits from VCAP_APPLICATION
        @SuppressWarnings("unchecked")
        Map<String, Object> limits = (Map<String, Object>) vcapApp.getOrDefault("limits", Collections.emptyMap());
        info.setMemoryLimitMb(toInt(limits.getOrDefault("mem", parseMemoryLimit())));
        info.setDiskLimitMb(toInt(limits.getOrDefault("disk", 1024)));

        // Current usage
        Runtime rt = Runtime.getRuntime();
        info.setMemoryUsedMb((rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
        File root = new File("/");
        long totalDisk = root.getTotalSpace() / (1024 * 1024);
        long freeDisk = root.getFreeSpace() / (1024 * 1024);
        info.setDiskUsedMb(totalDisk - freeDisk);

        return info;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBoundServices() {
        String vcapServices = System.getenv("VCAP_SERVICES");
        if (vcapServices == null || vcapServices.isBlank()) {
            return Collections.emptyList();
        }

        try {
            Map<String, List<Map<String, Object>>> services = objectMapper.readValue(
                    vcapServices, new TypeReference<>() {});

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : services.entrySet()) {
                for (Map<String, Object> svc : entry.getValue()) {
                    result.add(Map.of(
                            "name", svc.getOrDefault("name", "unknown"),
                            "label", svc.getOrDefault("label", entry.getKey()),
                            "plan", svc.getOrDefault("plan", "n/a"),
                            "tags", svc.getOrDefault("tags", List.of())
                    ));
                }
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> parseVcapApplication() {
        String vcapApp = System.getenv("VCAP_APPLICATION");
        if (vcapApp == null || vcapApp.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(vcapApp, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    private static int parseMemoryLimit() {
        String ml = System.getenv("MEMORY_LIMIT");
        if (ml != null) {
            try {
                return Integer.parseInt(ml.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {}
        }
        return 1024;
    }

    private static int toInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (NumberFormatException e) { return 0; }
    }
}
