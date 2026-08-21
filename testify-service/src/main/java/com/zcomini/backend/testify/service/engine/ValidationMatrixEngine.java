package com.zcomini.backend.testify.service.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.zcomini.backend.testify.dto.TestCaseWrapper;

@Component
public class ValidationMatrixEngine {

    @SuppressWarnings("unchecked")
    public List<TestCaseWrapper> buildTestSuite(Map<String, Object> basePayload,
            List<String> requiredFields,
            Map<String, Object> constraints) {
        List<TestCaseWrapper> suite = new ArrayList<>();
        List<String> keys = new ArrayList<>(basePayload.keySet());
        int n = keys.size();
        List<String> reqList = requiredFields != null ? requiredFields : new ArrayList<>();

        // Thuật toán Tập Lũy Thừa (Power Set) sinh hoán vị các trường dữ liệu
        if (n == 0) {
            suite.add(new TestCaseWrapper("Base Request - Không có payload", new HashMap<>(), 201));
        } else {
            for (int i = 1; i < (1 << n); i++) {
                Map<String, Object> currentPayload = new HashMap<>();
                boolean missingRequired = false;
                List<String> missingFieldsList = new ArrayList<>();

                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) > 0) {
                        currentPayload.put(keys.get(j), basePayload.get(keys.get(j)));
                    }
                }

                // Kiểm tra thiếu trường bắt buộc
                for (String req : reqList) {
                    if (!currentPayload.containsKey(req)) {
                        missingRequired = true;
                        missingFieldsList.add(req);
                    }
                }

                String baseDesc;
                int expectedStatus;
                if (missingRequired) {
                    baseDesc = "Thiếu trường bắt buộc: " + missingFieldsList;
                    expectedStatus = 400; // Bad Request
                } else if (currentPayload.size() == n) {
                    baseDesc = "Happy Path - Đầy đủ trường dữ liệu hợp lệ";
                    expectedStatus = 201; // Created / Success
                } else {
                    baseDesc = "Đủ trường bắt buộc, khuyết trường không bắt buộc";
                    expectedStatus = 201; // Success
                }

                // Lưu Test Case về mặt cấu trúc (Structural)
                suite.add(new TestCaseWrapper(baseDesc, new HashMap<>(currentPayload), expectedStatus));

                // Sinh Test Case nhúng giá trị sai (Constraints / Data)
                if (constraints != null && !missingRequired) {
                    final Map<String, Object> payloadForMapping = new HashMap<>(currentPayload);

                    constraints.forEach((fieldKey, configObj) -> {
                        if (payloadForMapping.containsKey(fieldKey)) {
                            Map<String, Object> config = (Map<String, Object>) configObj;

                            // Tiêm giá trị không hợp lệ (Enum sai, vượt Length, Regex sai...)
                            if (config.containsKey("invalid_values")) {
                                List<Object> invalidValues = (List<Object>) config.get("invalid_values");
                                for (Object invalidVal : invalidValues) {
                                    Map<String, Object> badPayload = new HashMap<>(payloadForMapping);
                                    badPayload.put(fieldKey, invalidVal);
                                    suite.add(new TestCaseWrapper(
                                            "Dữ liệu trường [" + fieldKey + "] không hợp lệ: " + invalidVal, badPayload,
                                            400));
                                }
                            }

                            // Tiêm giá trị trùng lặp
                            if (config.containsKey("duplicate_value")) {
                                Object dupValue = config.get("duplicate_value");
                                Map<String, Object> dupPayload = new HashMap<>(payloadForMapping);
                                dupPayload.put(fieldKey, dupValue);
                                // 409 Conflict hoặc 400 tùy API, ta dùng 409 theo chuẩn
                                suite.add(new TestCaseWrapper(
                                        "Dữ liệu trường [" + fieldKey + "] bị trùng lặp trong DB: " + dupValue,
                                        dupPayload, 409));
                            }
                        }
                    });
                }
            }
        }
        return suite;
    }
}
