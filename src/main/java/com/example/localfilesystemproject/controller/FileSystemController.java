package com.example.localfilesystemproject.controller;

import com.example.localfilesystemproject.dto.LoadAsDirectoryDto;
import com.example.localfilesystemproject.service.StorageService;
import com.example.localfilesystemproject.service.FilePathService;
import com.example.localfilesystemproject.storage.StorageFileNotFoundException;
import com.example.localfilesystemproject.storage.enumObj.OrderByDirectionEnum;
import com.example.localfilesystemproject.storage.enumObj.OrderByEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@RestController
public class FileSystemController {

    @Autowired
    StorageService storageService;

    @Autowired
    FilePathService filePathService;

    @RequestMapping(value = {
            "file",
            "file/{localFileSystemPath}/**"
            }, method=RequestMethod.GET)
    public ResponseEntity<?> serveFile(@PathVariable(required = false) String localFileSystemPath, HttpServletRequest request, @RequestParam(name = "orderBy", required = false) String orderBy, @RequestParam(name = "orderByDirection", required = false) String orderByDirection, @RequestParam(name = "filterByName", required = false) String filterByName) throws IOException {

        boolean orderByFlag = false;
        boolean orderByDirectionFlag = false;
        OrderByEnum[] orderByEnumArr = OrderByEnum.values();
        OrderByDirectionEnum[] orderByDirectionEnumArr = OrderByDirectionEnum.values();
        for (OrderByEnum value : orderByEnumArr) {
            if (value.getValue().equals(orderBy)) {
                orderByFlag = true;
                break;
            }
        }
        if (orderBy != null && !orderByFlag) {
            throw new IllegalArgumentException(
                    "Order Type: " + orderBy);
        }
        for (OrderByDirectionEnum value : orderByDirectionEnumArr) {
            if (value.getValue().equals(orderByDirection)) {
                orderByDirectionFlag = true;
                break;
            }
        }
        if (orderByDirection != null && !orderByDirectionFlag) {
            throw new IllegalArgumentException(
                    "Sort Direction: " + orderByDirection);
        }

        String requestURI = request.getRequestURI();
        String filePath = filePathService.arrangeFilePath(requestURI);
        Path pathLoad = storageService.load(filePath);
        File fileLoad = pathLoad.toFile();
        if (fileLoad.exists() && fileLoad.isFile()) {
            Resource file = storageService.loadAsResource(filePath);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } else if (fileLoad.exists() && fileLoad.isDirectory()) {
            LoadAsDirectoryDto loadAsDirectoryDto = storageService.loadAsDirectory(filePath, orderBy, orderByDirection, filterByName);
            return ResponseEntity.ok(loadAsDirectoryDto);
        } else {
            throw new StorageFileNotFoundException("Could not read file: " + filePath);
        }
    }

    @RequestMapping(value = {
            "file",
            "file/{localFileSystemPath}/**"
    }, method=RequestMethod.POST)
    public String handleFileUpload(@PathVariable(required = false) String localFileSystemPath, HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        String requestURI = request.getRequestURI();
        String filePath = filePathService.arrangeFilePath(requestURI).equals("") ? "" : filePathService.arrangeFilePath(requestURI) + "/";
        Boolean rs = storageService.isEmptyFile(file);
        if (rs) {
            return "File empty!!";
        }
        rs = storageService.isExistedFile(filePath + file.getOriginalFilename());
        if (rs) {
            return "File existed not allow to operate";
        }

        Path pathLoad = storageService.load(filePath);
        File fileLoad = pathLoad.toFile();
        if (fileLoad.exists() && fileLoad.isDirectory()) {
            storageService.store(file, filePath);
        } else {
            throw new StorageFileNotFoundException("Could not read file: " + filePath);
        }

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");
        return "File is Uploaded";
    }

    @RequestMapping(value = {
            "file",
            "file/{localFileSystemPath}/**"
    }, method=RequestMethod.PATCH)
    public String handleFileUpdate(@PathVariable(required = false) String localFileSystemPath, HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws IOException {
        String requestURI = request.getRequestURI();
        String filePath = filePathService.arrangeFilePath(requestURI).equals("") ? "" : filePathService.arrangeFilePath(requestURI) + "/";

        Boolean rs = storageService.isEmptyFile(file);
        if (rs) {
            return "File empty!!";
        }
        rs = storageService.isExistedFile(file.getOriginalFilename());
        if (!rs) {
            return "File is not existed not allow to operate";
        }

        Path pathLoad = storageService.load(filePath);
        File fileLoad = pathLoad.toFile();
        if (fileLoad.exists() && fileLoad.isDirectory()) {
            storageService.update(file, filePath);
        } else {
            throw new StorageFileNotFoundException("Could not read file: " + filePath);
        }

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");
        return "File is Updated";
    }

    @DeleteMapping("file/{localSystemFilePath}/**")
    public String handleFileDelete(@PathVariable(required = false) String localSystemFilePath, HttpServletRequest request) throws IOException {
        String requestURI = request.getRequestURI();
        String filePath = filePathService.arrangeFilePath(requestURI);
        storageService.deleteFile(filePath);
        return "File is Deleted";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
