package com.beeacademy.backend.api.media;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.media.UploadMediaUseCase;
import com.beeacademy.backend.domain.media.MediaSignature;
import com.beeacademy.backend.domain.media.MediaUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("MediaController MockMvc Tests")
class MediaControllerTest {

    private MockMvc mockMvc;
    private UploadMediaUseCase uploadMediaUseCase;

    @BeforeEach
    void setUp() {
        uploadMediaUseCase = Mockito.mock(UploadMediaUseCase.class);
        MediaController controller = new MediaController(uploadMediaUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/media/upload/image -> Tải ảnh thành công trả về secure URL")
    void uploadImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "course_thumb.png", "image/png", "fake image content".getBytes()
        );

        MediaUploadResult result = new MediaUploadResult(
                "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                "beeacademy/courses/thumbnails/sample",
                "image", "jpg", 1024, null
        );

        when(uploadMediaUseCase.uploadCourseThumbnail(any(), any())).thenReturn(result);

        mockMvc.perform(multipart("/api/media/upload/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value("https://res.cloudinary.com/demo/image/upload/sample.jpg"))
                .andExpect(jsonPath("$.data.publicId").value("beeacademy/courses/thumbnails/sample"));
    }

    @Test
    @DisplayName("POST /api/media/upload/video -> Tải video thành công trả về URL và durationSec")
    void uploadVideo_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "lesson.mp4", "video/mp4", "fake video content".getBytes()
        );

        MediaUploadResult result = new MediaUploadResult(
                "https://res.cloudinary.com/demo/video/upload/lesson1.mp4",
                "beeacademy/courses/videos/lesson1",
                "video", "mp4", 1048576, 720
        );

        when(uploadMediaUseCase.uploadLessonVideo(any(), any())).thenReturn(result);

        mockMvc.perform(multipart("/api/media/upload/video").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value("https://res.cloudinary.com/demo/video/upload/lesson1.mp4"))
                .andExpect(jsonPath("$.data.durationSec").value(720));
    }

    @Test
    @DisplayName("GET /api/media/signature -> Trả về chữ ký số Signed Upload")
    void getUploadSignature_Success() throws Exception {
        MediaSignature signature = new MediaSignature(
                "abcdef123456", 1700000000L, "test_api_key", "bee-academy", "beeacademy/courses/videos"
        );

        when(uploadMediaUseCase.getUploadSignature(any(), any())).thenReturn(signature);

        mockMvc.perform(get("/api/media/signature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.signature").value("abcdef123456"))
                .andExpect(jsonPath("$.data.apiKey").value("test_api_key"));
    }
}
