package capstone.cycle.file.dto;

import capstone.cycle.file.entity.File;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProfileDTO {
    private Long id;

    @Schema(description = "파일의 원본 이름")
    private String originalName;

    @Schema(description = "파일의 저장 경로")
    private String path;

    @Schema(description = "파일의 MIME 타입")
    private String contentType;

    private long size;

    private String extension;

    @Schema(description = "파일의 체크섬")
    private String checksum;

    public static ProfileDTO from(File file) {
        return ProfileDTO.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .path(file.getPath())
                .contentType(file.getContentType())
                .size(file.getSize())
                .extension(file.getExtension())
                .checksum(file.getChecksum())
                .build();
    }
}
