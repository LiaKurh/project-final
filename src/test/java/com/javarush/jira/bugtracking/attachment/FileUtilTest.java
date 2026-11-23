package com.javarush.jira.bugtracking.attachment;

import com.javarush.jira.common.error.IllegalRequestDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {
    private static final String FILE_NAME = "test.txt";
    private static final String NON_EXISTENT_FILE_NAME = "non-existent.txt";
    private static final String CONTENT = "JiraRush";

    @TempDir
    Path tempDir;

    @Test
    void uploadSuccess() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile("file", FILE_NAME,
                "text/plain", CONTENT.getBytes());
        Path directoryPath = tempDir.resolve("nested/dir");
        FileUtil.upload(multipartFile, directoryPath.toString(), FILE_NAME);
        Path uploadedFilePath = directoryPath.resolve(FILE_NAME);
        assertTrue(Files.exists(uploadedFilePath));
        assertEquals(CONTENT, Files.readString(uploadedFilePath));
    }

    @Test
    void uploadEmptyFileThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", FILE_NAME,
                "text/plain", new byte[0]);
        IllegalRequestDataException exception = assertThrows(IllegalRequestDataException.class, () ->
                FileUtil.upload(emptyFile, tempDir.toString(), FILE_NAME));
        assertEquals("Select a file to upload.", exception.getMessage());
    }

    @Test
    void uploadToInvalidDirectoryThrowsException() throws Exception {
        Path existingFileAsDir = createTestFile();
        MockMultipartFile multipartFile = new MockMultipartFile("file", FILE_NAME,
                "text/plain", CONTENT.getBytes());

        IllegalRequestDataException exception = assertThrows(IllegalRequestDataException.class, () ->
                FileUtil.upload(multipartFile, existingFileAsDir.toString(), FILE_NAME));
        assertEquals("Failed to upload file " + multipartFile.getOriginalFilename(), exception.getMessage());
    }

    @Test
    void downloadSuccess() throws Exception {
        Path filePath = createTestFile();
        Resource resource = FileUtil.download(filePath.toString());

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(FILE_NAME, resource.getFilename());
    }

    @Test
    void downloadNotFoundThrowsException() {
        Path nonExistentFile = tempDir.resolve(NON_EXISTENT_FILE_NAME);
        IllegalRequestDataException exception = assertThrows(IllegalRequestDataException.class, () ->
                FileUtil.download(nonExistentFile.toString()));
        assertEquals("Failed to download file " + NON_EXISTENT_FILE_NAME, exception.getMessage());
    }

    @Test
    void deleteSuccess() throws Exception {
        Path fileToDelete = createTestFile();
        assertTrue(Files.exists(fileToDelete));

        FileUtil.delete(fileToDelete.toString());
        assertFalse(Files.exists(fileToDelete));
    }

    private Path createTestFile() throws IOException {
        Path filePath = tempDir.resolve(FILE_NAME);
        Files.writeString(filePath, CONTENT);
        return filePath;
    }
}
