package com.example.localfilesystemproject;

import com.example.localfilesystemproject.dto.LoadAsDirectoryDto;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
class LocalFileSystemProjectApplicationTests {

    @Autowired
    private MockMvc mvc;

    Path rootLocation;

    @Test
    void contextLoads() {
    }

    @Test
    void testGetFileNotExisted() throws IOException {
        // Given
        HttpUriRequest requestNotExisted = new HttpGet("http://localhost:8080/file/aaa.txt");

        // When
        HttpResponse httpResponseNotExisted = HttpClientBuilder.create().build().execute(requestNotExisted);

        //Then
        Assertions.assertEquals(httpResponseNotExisted.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void testGetFileExisted() throws IOException {
        // Given
        HttpUriRequest requestExisted = new HttpGet("http://localhost:8080/file/e136203eff1017a1.jpeg");

        // When
        HttpResponse httpResponseExisted = HttpClientBuilder.create().build().execute(requestExisted);

        //Then
        Assertions.assertEquals(httpResponseExisted.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    void testGetFileDirectory() throws IOException {
        // Given
        LoadAsDirectoryDto loadAsDirectoryDto = new LoadAsDirectoryDto();
        loadAsDirectoryDto.setIsDirectory(true);
        HttpUriRequest requestDirectory = new HttpGet("http://localhost:8080/file");
        HttpUriRequest requestDirectoryNotExisted = new HttpGet("http://localhost:8080/file/aaa");

        // When
        HttpResponse httpResponseDirectory = HttpClientBuilder.create().build().execute(requestDirectory);
        HttpResponse httpResponseDirectoryNotExisted = HttpClientBuilder.create().build().execute(requestDirectoryNotExisted);

        //Then
        Assertions.assertEquals(httpResponseDirectory.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        System.out.print(httpResponseDirectory.getClass());
//        Assertions.assertEquals(httpResponseDirectory.getEntity());
        Assertions.assertEquals(httpResponseDirectoryNotExisted.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
    }

//    @Test
//    void testGetFileWithFileExisted() throws IOException {
//        // Given
//        HttpUriRequest request = new HttpGet("http://localhost:8080/file/aaa.txt");
//
//        // When
//        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
//
//        //Then
//        LoadAsDirectoryDto
//        Assertions.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
//    }



    @Test
    public void testUploadFile() throws Exception {
        Path path = Paths.get("./e136203eff1017a1.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("file", path.getFileName().toString(), "form/data", "Some bytes".getBytes());

        mvc.perform(MockMvcRequestBuilders.multipart("/file")
                        .file(multipartFile))
                        .andExpect(status().is(200))
                        .andExpect(content().string("File is Uploaded"));
    }

    @Test
    public void testUpdateFileNotExisted() throws Exception {
        Path path = Paths.get("/19536cd34be07e0e2c8fea27ef4ddf2d.png");
        MockMultipartFile multipartFile = new MockMultipartFile("file", path.getFileName().toString(), "form/data", "Some bytes".getBytes());

        mvc.perform(MockMvcRequestBuilders.multipart("/file")
                        .file(multipartFile))
                        .andExpect(status().is(200))
                        .andExpect(content().string("File is not existed not allow to operate"));
    }

}
