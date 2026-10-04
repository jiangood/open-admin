package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface SysFileRepository extends BaseRepository<SysFile, String> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SysFile f SET f.joinTable = :joinTable, f.joinId = :joinId, f.status = io.github.jiangood.openadmin.framework.enums.FileStatus.IN_USE " +
            "WHERE f.objectName IN :objectNames " +
            "AND (f.joinId IS NULL OR (f.joinTable = :joinTable AND f.joinId = :joinId))")
    int updateJoinRefByObjectNames(@Param("joinTable") String joinTable, @Param("joinId") String joinId, @Param("objectNames") Collection<String> objectNames);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SysFile f SET f.status = :status WHERE f.objectName IN :objectNames")
    int updateStatusByObjectNames(@Param("objectNames") Collection<String> objectNames, @Param("status") FileStatus status);

    @Query("SELECT f FROM SysFile f WHERE f.objectName IN :objectNames")
    List<SysFile> findByObjectNameIn(@Param("objectNames") Collection<String> objectNames);

    @Query("SELECT f FROM SysFile f WHERE f.objectName = :objectName")
    SysFile findByObjectName(@Param("objectName") String objectName);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SysFile f SET f.status = :to WHERE f.status = :from AND f.createTime < :deadline")
    int updateStatusByStatusAndCreateTimeBefore(@Param("from") FileStatus from, @Param("to") FileStatus to, @Param("deadline") LocalDateTime deadline);

    List<SysFile> findByStatus(FileStatus status);

    Page<SysFile> findByStatus(FileStatus status, Pageable pageable);
}
