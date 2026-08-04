package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface SysFileRepository extends BaseRepository<SysFile, String> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SysFile f SET f.joinTable = :joinTable, f.joinId = :joinId, f.status = io.github.jiangood.openadmin.framework.enums.FileStatus.IN_USE WHERE f.objectName IN :objectNames")
    int updateJoinRefByObjectNames(@Param("joinTable") String joinTable, @Param("joinId") String joinId, @Param("objectNames") Collection<String> objectNames);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SysFile f SET f.status = :status WHERE f.objectName IN :objectNames")
    int updateStatusByObjectNames(@Param("objectNames") Collection<String> objectNames, @Param("status") FileStatus status);

    @Query("SELECT f FROM SysFile f WHERE f.objectName IN :objectNames")
    List<SysFile> findByObjectNameIn(@Param("objectNames") Collection<String> objectNames);

    @Query("SELECT f FROM SysFile f WHERE f.objectName = :objectName")
    SysFile findByObjectName(@Param("objectName") String objectName);

    List<SysFile> findByStatus(FileStatus status);

    List<SysFile> findByStatusAndCreateTimeBefore(FileStatus status, Date deadline);

    @Query("SELECT f FROM SysFile f WHERE f.joinTable IS NULL AND f.joinId IS NULL AND f.createTime < :deadline")
    List<SysFile> findUnclaimedFiles(@Param("deadline") Date deadline);

    @Query("SELECT f FROM SysFile f WHERE f.joinTable IS NOT NULL AND f.joinId IS NOT NULL")
    List<SysFile> findClaimedFiles();
}
