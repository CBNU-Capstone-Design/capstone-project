package capstone.cycle.file.dto;

import capstone.cycle.file.entity.File;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Slf4j
public class FileDTO {

    private Long id;
    private String name;
    private String originalName;
    private String path;
    private String contentType;
    private long size;
    private String extension;
    private String checksum;
    private LocalDateTime createdAt;

    public FileDTO(File file) {
        this.id = file.getId();
        this.name = file.getName();
        this.originalName = file.getOriginalName();
        this.path = file.getPath();
        this.contentType = file.getContentType();
        this.size = file.getSize();
        this.extension = file.getExtension();
        this.checksum = file.getChecksum();
        this.createdAt = file.getCreatedAt();
    }

    public static FileDTO from(File file) {
        return FileDTO.builder()
                .id(file.getId())
                .name(file.getName())
                .originalName(file.getOriginalName())
                .path(file.getPath())
                .contentType(file.getContentType())
                .size(file.getSize())
                .extension(file.getExtension())
                .checksum(file.getChecksum())
                .createdAt(file.getCreatedAt())
                .build();
    }
}
