package net.virtualboss.application.web.controller.v1;

import net.virtualboss.application.service.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MigrationControllerIT  extends BaseIntegrationTest {
    private static final String TEST_DATA_FILE_NAME = "src/test/resources/SolidBuildersDatabaseFixed.zip";
    private static final String TEST_DATA_NON_ZIP_FILE_NAME = "src/test/resources/NonZipFile.zip";

    @BeforeEach
    void initBeforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("test upload zip data to migrate")
    void testZipUpload() throws Exception {

        MockMultipartFile zipFile = new MockMultipartFile(
                "file",
                "data.zip",
                "application/zip",
                new FileInputStream(TEST_DATA_FILE_NAME)
        );

        mockMvc.perform(multipart("/upload")
                        .file(zipFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Data added"));
    }

    @Test
    @DisplayName("test upload non zip file data to migrate")
    void testZipUploadNonZipFile() throws Exception {

        MockMultipartFile zipFile = new MockMultipartFile(
                "file",
                "data.zip",
                "application/zip",
                new FileInputStream(TEST_DATA_NON_ZIP_FILE_NAME)
        );

        mockMvc.perform(multipart("/upload")
                        .file(zipFile))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("test migrate test data")
    void testMigrateTestData() throws Exception {

        mockMvc.perform(get("/migrate"))
                .andExpect(status().isOk());
    }

}