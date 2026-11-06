package com.beeacademy.backend.application.media;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.media.MediaSignature;
import com.beeacademy.backend.domain.media.MediaStoragePort;
import com.beeacademy.backend.domain.media.MediaUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("UploadMediaUseCase Unit Tests")
class UploadMediaUseCaseTest {

    private MediaStoragePort mediaStoragePort;
    private UploadMediaUseCase useCase;

    @BeforeEach
    void setUp() {
        mediaStoragePort = Mockito.mock(MediaStoragePort.class);
        useCase = new UploadMediaUseCase(mediaStoragePort);
    }

    @Test
    @DisplayName("Upload ảnh thumbnail thành công")
    void uploadCourseThumbnail_Success() {
        byte[] dummyBytes = "image content".getBytes();
        MediaUploadResult expected = new MediaUploadResult(
                "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                "beeacademy/courses/thumbnails/sample",
                "image",
                "jpg",
                dummyBytes.length,
                null
        );

        when(mediaStoragePort.uploadImage(eq(dummyBytes), eq("thumb.jpg"), any())).thenReturn(expected);

        MediaUploadResult result = useCase.uploadCourseThumbnail(dummyBytes, "thumb.jpg");

        assertThat(result).isNotNull();
        assertThat(result.url()).isEqualTo("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        assertThat(result.publicId()).isEqualTo("beeacademy/courses/thumbnails/sample");
    }

    @Test
    @DisplayName("Upload video bài giảng thành công kèm thời lượng durationSec")
    void uploadLessonVideo_Success() {
        byte[] dummyBytes = "video content".getBytes();
        MediaUploadResult expected = new MediaUploadResult(
                "https://res.cloudinary.com/demo/video/upload/lesson1.mp4",
                "beeacademy/courses/videos/lesson1",
                "video",
                "mp4",
                dummyBytes.length,
                900
        );

        when(mediaStoragePort.uploadVideo(eq(dummyBytes), eq("lesson1.mp4"), any())).thenReturn(expected);

        MediaUploadResult result = useCase.uploadLessonVideo(dummyBytes, "lesson1.mp4");

        assertThat(result).isNotNull();
        assertThat(result.url()).isEqualTo("https://res.cloudinary.com/demo/video/upload/lesson1.mp4");
        assertThat(result.durationSec()).isEqualTo(900);
    }

    @Test
    @DisplayName("Upload file rỗng ném ngoại lệ EMPTY_FILE")
    void uploadEmptyFile_ThrowsException() {
        assertThatThrownBy(() -> useCase.uploadCourseThumbnail(new byte[0], "empty.jpg"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("không được rỗng");
    }

    @Test
    @DisplayName("Lấy chữ ký số Signed Upload cho client thành công")
    void getUploadSignature_Success() {
        MediaSignature expected = new MediaSignature("sig123", 123456789L, "key123", "bee-academy", "beeacademy/courses/videos");
        when(mediaStoragePort.generateUploadSignature(any(), any())).thenReturn(expected);

        MediaSignature signature = useCase.getUploadSignature("beeacademy/courses/videos", "video");

        assertThat(signature).isNotNull();
        assertThat(signature.signature()).isEqualTo("sig123");
        assertThat(signature.apiKey()).isEqualTo("key123");
    }
}
