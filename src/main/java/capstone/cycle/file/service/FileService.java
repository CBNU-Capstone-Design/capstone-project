package capstone.cycle.file.service;

import capstone.cycle.file.entity.File;
import capstone.cycle.post.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface FileService {

    File uploadFile(Post post,MultipartFile file);

    List<File> uploadFiles(Post post,List<MultipartFile> files);

    void deleteFile(Long fileId);

    void deleteFiles(List<File> files);

    void deleteFilesByFileIds(List<Long> fileIds);

    File getFile(Long fileId);
}
