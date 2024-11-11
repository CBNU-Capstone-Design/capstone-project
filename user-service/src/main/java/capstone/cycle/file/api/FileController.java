package capstone.cycle.file.api;

import capstone.cycle.file.entity.File;
import capstone.cycle.file.error.FileErrorResult;
import capstone.cycle.file.error.FileException;
import capstone.cycle.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileService fileService;

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> serveFile(@PathVariable Long fileId) throws IOException {
        File file = fileService.getFile(fileId);

        Path filePath = Paths.get(uploadDir).resolve(file.getName());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getOriginalName() + "\"")
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(resource);
        } else {
            throw new FileException(FileErrorResult.FILE_NOT_FOUND);
        }
    }
}
