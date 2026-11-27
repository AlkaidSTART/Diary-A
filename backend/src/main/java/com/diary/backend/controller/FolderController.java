package com.diary.backend.controller;

import com.diary.backend.common.Result;
import com.diary.backend.entity.Folder;
import com.diary.backend.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @GetMapping
    public Result<List<Folder>> getUserFolders(@RequestParam Long userId) {
        return folderService.getUserFolders(userId);
    }

    @PostMapping
    public Result<Folder> createFolder(@RequestBody Map<String, Object> payload) {
        if (payload == null) {
            return Result.error(400, "Request body is required");
        }
        Object userIdObj = payload.get("userId");
        Object nameObj = payload.get("name");
        
        if (userIdObj == null) {
            return Result.error(400, "User ID is required");
        }
        if (!(nameObj instanceof String name) || name.isBlank()) {
            return Result.error(400, "Folder name is required");
        }
        
        Long userId;
        try {
            userId = Long.valueOf(userIdObj.toString());
        } catch (NumberFormatException e) {
            return Result.error(400, "Invalid user ID");
        }
        return folderService.createFolder(userId, name.trim());
    }

    @PutMapping("/{id}")
    public Result<Void> updateFolder(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        if (payload == null) {
            return Result.error(400, "Request body is required");
        }
        Object userIdObj = payload.get("userId");
        Object nameObj = payload.get("name");
        
        if (userIdObj == null) {
            return Result.error(400, "User ID is required");
        }
        if (!(nameObj instanceof String name) || name.isBlank()) {
            return Result.error(400, "Folder name is required");
        }
        
        Long userId;
        try {
            userId = Long.valueOf(userIdObj.toString());
        } catch (NumberFormatException e) {
            return Result.error(400, "Invalid user ID");
        }
        return folderService.updateFolder(userId, id, name.trim());
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFolder(@PathVariable String id, @RequestParam Long userId) {
        return folderService.deleteFolder(userId, id);
    }
}
