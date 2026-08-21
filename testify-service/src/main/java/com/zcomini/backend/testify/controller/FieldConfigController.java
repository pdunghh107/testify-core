package com.zcomini.backend.testify.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zcomini.backend.shared.api.dto.MessageResponse;
import com.zcomini.backend.testify.dto.request.CreateFieldConfigRequest;
import com.zcomini.backend.testify.dto.request.UpdateFieldConfigRequest;
import com.zcomini.backend.testify.dto.response.FieldConfigResponse;
import com.zcomini.backend.testify.service.FieldConfigService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/field-configs")
@RequiredArgsConstructor
public class FieldConfigController {

    private final FieldConfigService fieldConfigService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FieldConfigResponse createFieldConfig(@RequestBody CreateFieldConfigRequest request) {
        return fieldConfigService.createFieldConfig(request);
    }

    @GetMapping("/workspaces/{workspaceId}")
    public List<FieldConfigResponse> getFieldConfigsByWorkspace(@PathVariable UUID workspaceId) {
        return fieldConfigService.getFieldConfigsByWorkspace(workspaceId);
    }

    @PutMapping("/{id}")
    public FieldConfigResponse updateFieldConfig(@PathVariable UUID id, @RequestBody UpdateFieldConfigRequest request) {
        return fieldConfigService.updateFieldConfig(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse deleteFieldConfig(@PathVariable UUID id) {
        fieldConfigService.deleteFieldConfig(id);
        return new MessageResponse("Xóa cấu hình field thành công.");
    }
}
