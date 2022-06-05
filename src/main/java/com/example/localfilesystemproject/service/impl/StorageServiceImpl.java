package com.example.localfilesystemproject.service.impl;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;
import com.example.localfilesystemproject.dto.LoadAsDirectoryDto;
import com.example.localfilesystemproject.service.StorageService;
import com.example.localfilesystemproject.storage.StorageException;
import com.example.localfilesystemproject.storage.StorageFileNotFoundException;
import com.example.localfilesystemproject.storage.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {

    Path rootLocation;

    @Autowired
    public StorageServiceImpl(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    @Async("executor")
    public void store(MultipartFile file, String filePath) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path path;
            if (filePath.equals("")) {
                path = this.rootLocation;
            } else {
                path = this.rootLocation.resolve(filePath);
            }
            Path destinationFile = path.resolve(
                            Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(path.toAbsolutePath())) {
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    @Async("executor")
    public void update(MultipartFile file, String filePath) throws IOException {
        deleteFile(filePath + file.getOriginalFilename());
        store(file, filePath);
    }

    @Override
    @Async("executor")
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 5)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        if (filename.equals(null) || filename.equals("")) {
            return rootLocation.resolve("");
        } else {
            return rootLocation.resolve(filename);
        }
    }

    @Override
    @Async("executor")
    public Boolean isExistedFile(String filename) {
        Boolean rs = false;
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                rs = true;
            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
        return rs;
    }

    @Override
    @Async("executor")
    public Boolean isEmptyFile(MultipartFile file) {
        Boolean rs = false;
        if (file.isEmpty()) {
            rs = true;
            throw new StorageFileNotFoundException("Could not read file");
        }
        return rs;
    }

    @Override
    @Async("executor")
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    @Async("executor")
    public LoadAsDirectoryDto loadAsDirectory(String filename, String orderBy, String orderByDirection, String filterName) {
        List<String> listDir = new ArrayList<String>();
        LoadAsDirectoryDto loadAsDirectoryDto = new LoadAsDirectoryDto();
        Path pathLoad = load(filename);
        File fileLoad = pathLoad.toFile();
        if ((filterName != null && filterName != "")) {
            listDir = fileArrange(fileLoad, filterName, true);
        } else if ((orderBy != null && orderBy != "") && (orderByDirection != null && orderByDirection != "")) {
            listDir = fileOrder(fileLoad, orderBy, orderByDirection);
        } else {
            listDir = fileArrange(fileLoad, filterName, false);
        }
        loadAsDirectoryDto.setIsDirectory(true);
        loadAsDirectoryDto.setFiles(listDir);
        return loadAsDirectoryDto;
    }

    @Override
    @Async("executor")
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    @Async("executor")
    public void deleteFile(String filename) throws IOException {
        try {
            Path file = load(filename);
            FileSystemUtils.deleteRecursively(file);
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    @Async("executor")
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    private List<String> fileArrange(File fileLoad, String filterName, Boolean filterNameFlag) {
        List<String> fileList = new ArrayList<String>();
        File[] fileArr;
        if (filterNameFlag) {
            String nameFilt = filterName;
            FilenameFilter fileNameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if(name.lastIndexOf(nameFilt)>=0)
                    {
                        int lastIndex = name.lastIndexOf(nameFilt);

                        String str = name.substring(lastIndex);

                        if(str.length() > 0)
                        {
                            return true;
                        }
                    }
                    return false;
                }
            };
            fileArr = fileLoad.listFiles(fileNameFilter);
        } else {
            fileArr = fileLoad.listFiles();
        }
        if(fileArr != null){
            for (int i=0; i < fileArr.length; i++) {
                if (fileArr[i].isDirectory()) {
                    fileList.add(fileArr[i].getName() + "/");
                } else {
                    fileList.add(fileArr[i].getName());
                }
            }
        }
        return fileList;
    }

    private List<String> fileOrder(File fileLoad, String orderBy, String orderByDirection) {
        List<String> fileList = new ArrayList<String>();
        switch (orderBy) {
            case "lastModified" : {
                fileList = dateSort(fileLoad, orderByDirection);
                break;
            }
            case "size" : {
                fileList = sizeSort(fileLoad, orderByDirection);
                break;
            }
            case "fileName" : {
                fileList = fileNameSort(fileLoad, orderByDirection);
                break;
            }
            default : {
                break;
            }
        }
        return fileList;
    }

    private List<String> fileNameSort(File fileLoad, String orderByDirection) {
        List<String> fileList = new ArrayList<String>();
        File[] fileArr = fileLoad.listFiles();
        if(fileArr != null){
            for (int i=0; i < fileArr.length; i++) {
                if (fileArr[i].isDirectory()) {
                    fileList.add(fileArr[i].getName() + "/");
                } else {
                    fileList.add(fileArr[i].getName());
                }
            }
        }
        switch (orderByDirection) {
            case "Ascending" : {
                Collections.sort(fileList);
                break;
            }
            case "Descending" : {
                Collections.sort(fileList,Collections.reverseOrder());
                break;
            }
            default : {
                break;
            }
        }
        return fileList;
    }

    private List<String> dateSort(File fileLoad, String orderByDirection) {
        List<String> fileList = new ArrayList<String>();
        File[] fileArr = fileLoad.listFiles();
        switch (orderByDirection) {
            case "Ascending" : {
                Arrays.sort(fileArr, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
                break;
            }
            case "Descending" : {
                Arrays.sort(fileArr, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                break;
            }
            default : {
                break;
            }
        }
        for (int i=0; i < fileArr.length; i++) {
            if (fileArr[i].isDirectory()) {
                fileList.add(fileArr[i].getName() + "/");
            } else {
                fileList.add(fileArr[i].getName());
            }
        }
        return fileList;
    }

    private List<String> sizeSort(File fileLoad, String orderByDirection) {
        List<String> fileList = new ArrayList<String>();
        File[] fileArr = fileLoad.listFiles();
        switch (orderByDirection) {
            case "Ascending" : {
                Arrays.sort(fileArr, SizeFileComparator.SIZE_SUMDIR_COMPARATOR);
                break;
            }
            case "Descending" : {
                Arrays.sort(fileArr, SizeFileComparator.SIZE_SUMDIR_REVERSE);
                break;
            }
            default : {
                break;
            }
        }
        for (int i=0; i < fileArr.length; i++) {
            if (fileArr[i].isDirectory()) {
                fileList.add(fileArr[i].getName() + "/");
            } else {
                fileList.add(fileArr[i].getName());
            }
        }
        return fileList;
    }
}
