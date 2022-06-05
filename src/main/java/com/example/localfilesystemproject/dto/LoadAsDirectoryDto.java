package com.example.localfilesystemproject.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoadAsDirectoryDto {

    Boolean isDirectory;

    List<String> files;
}
