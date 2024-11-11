package capstone.cycle.file.service;

import capstone.cycle.file.entity.File;
import capstone.cycle.file.error.FileErrorResult;
import capstone.cycle.file.error.FileException;
import capstone.cycle.file.repository.FileRepository;
import capstone.cycle.post.entity.Post;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    private final Storage storage;
    @Value("${cloud.gcp.storage.bucket}")
    private String bucketName;
    @Value("${google.cloud.storage.url}")
    private String bucketUrl;


    @Override
    @Transactional
    public File uploadFile(Post post, MultipartFile file) {
        try {

            String uuid = UUID.randomUUID().toString();
            String ext = file.getContentType();
            storage.create(BlobInfo.newBuilder(bucketName, uuid).setContentType(ext).build(), file.getBytes());

            String fileName = generateFileName(file);
            String targetLocation = bucketUrl + uuid;

            File fileEntity = createFileEntity(file, targetLocation, fileName, post);

            return fileRepository.save(fileEntity);
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new FileException(FileErrorResult.UPLOAD_FAIL);
        }
    }

    @Override
    @Transactional
    public List<File> uploadFiles(Post post, List<MultipartFile> files) {
        return files.stream()
                .map((file)->uploadFile(post,file))
                .toList();
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        File file = getFile(fileId);
        try {
            Files.deleteIfExists(Paths.get(file.getPath()));
            fileRepository.delete(file);
            log.info("Successfully deleted file with ID: {}", fileId);
        } catch (IOException e) {
            log.error("File deletion failed for file ID: {}", fileId, e);
            throw new FileException(FileErrorResult.DELETE_FAIL);
        }
    }

    @Override
    public void deleteFiles(List<File> files) {
        fileRepository.deleteAll(files);
    }

    @Override
    public void deleteFilesByFileIds(List<Long> fileIds) {
        fileRepository.deleteFilesByIds(fileIds);
    }

    @Override
    public File getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileException(FileErrorResult.FILE_NOT_FOUND));
    }

    private String generateFileName(MultipartFile file) {
        return UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
    }


    private File createFileEntity(MultipartFile file, String targetLocation, String fileName, Post post) throws IOException {
        return File.createFile(
                fileName,
                file.getOriginalFilename(),
                targetLocation,
                file.getContentType(),
                file.getSize(),
                getFileExtension(file.getOriginalFilename()),
                calculateChecksum(file.getInputStream()),
                post
        );
    }


    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String calculateChecksum(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return calculateChecksum(is);
        }
    }

    private String calculateChecksum(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
