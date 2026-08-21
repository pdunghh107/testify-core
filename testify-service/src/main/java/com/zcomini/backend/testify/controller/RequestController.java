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
import com.zcomini.backend.testify.dto.request.CreateRequestDto;
import com.zcomini.backend.testify.dto.request.UpdateRequestDto;
import com.zcomini.backend.testify.dto.response.RequestResponse;
import com.zcomini.backend.testify.service.RequestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponse createRequest(@RequestBody CreateRequestDto request) {
        return requestService.createRequest(request);
    }

    @GetMapping("/{id}")
    public RequestResponse getRequestById(@PathVariable UUID id) {
        return requestService.getRequestById(id);
    }

    @GetMapping("/workspaces/{workspaceId}")
    public List<RequestResponse> getRequestsByWorkspace(@PathVariable UUID workspaceId) {
        return requestService.getRequestsByWorkspace(workspaceId);
    }

    @PutMapping("/{id}")
    public RequestResponse updateRequest(@PathVariable UUID id, @RequestBody UpdateRequestDto request) {
        return requestService.updateRequest(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse deleteRequest(@PathVariable UUID id) {
        requestService.deleteRequest(id);
        return new MessageResponse("Deleted successfully");
    }
}
