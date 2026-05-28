package mype_backend.controller;

import mype_backend.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        
        String url = storageService.uploadFile(file, folder);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        
        return ResponseEntity.ok(response);
    }
}
