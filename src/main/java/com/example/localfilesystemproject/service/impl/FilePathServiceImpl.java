package com.example.localfilesystemproject.service.impl;

import com.example.localfilesystemproject.service.FilePathService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

@Service
public class FilePathServiceImpl implements FilePathService {
    @Override
    public String arrangeFilePath(String requestURIStr) {
        String[] requestURIArr = requestURIStr.split("/");
        List<String> list = new ArrayList<String>(Arrays.asList(requestURIArr));
        list.remove("");
        list.remove("file");
        requestURIArr = list.toArray(new String[0]);
        StringJoiner joiner = new StringJoiner("/");
        for(int i = 0; i < requestURIArr.length; i++) {
            joiner.add(requestURIArr[i]);
        }
        String str = joiner.toString();
        return str;
    }
}
