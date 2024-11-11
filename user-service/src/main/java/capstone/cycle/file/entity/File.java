package capstone.cycle.file.entity;

import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.dto.ProfileDTO;
import capstone.cycle.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String originalName;
    private String path;
    private String contentType;
    private long size;
    private String extension;
    private String checksum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static File createFile(String name, String originalName, String path,
                                  String contentType, long size, String extension, String checksum, Post post) {
        return File.builder()
                .name(name)
                .originalName(originalName)
                .path(path)
                .contentType(contentType)
                .size(size)
                .extension(extension)
                .checksum(checksum)
                .post(post)
                .build();
    }
    public FileDTO toDTO() {
        return FileDTO.builder()
                .id(this.id)
                .name(this.name)
                .originalName(this.originalName)
                .path(this.path)
                .contentType(this.contentType)
                .size(this.size)
                .extension(this.extension)
                .checksum(this.checksum)
                .createdAt(this.createdAt)
                .build();
    }

    public ProfileDTO toProfileDTO() {
        return ProfileDTO.builder()
                .id(this.id)
                .originalName(this.originalName)
                .path(this.path)
                .contentType(this.contentType)
                .size(this.size)
                .extension(this.extension)
                .checksum(this.checksum)
                .build();
    }
}
