package com.example.localfilesystemproject.service;

import com.example.localfilesystemproject.dto.LoadAsDirectoryDto;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file, String path);

    void update(MultipartFile file, String filePath) throws IOException;

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    LoadAsDirectoryDto loadAsDirectory(String filename, String orderBy, String orderByDirection, String filterName);

    Boolean isExistedFile(String filename);

    Boolean isEmptyFile(MultipartFile file);

    void deleteAll();

    void deleteFile(String filename) throws IOException;
}
