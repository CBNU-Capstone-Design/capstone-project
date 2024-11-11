package capstone.cycle.file.repository;

import capstone.cycle.file.entity.File;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FileRepository extends CrudRepository<File, Long> {

    @Modifying
    @Query("delete from File f where f.id in :ids")
    void deleteFilesByIds(List<Long> ids);
}
