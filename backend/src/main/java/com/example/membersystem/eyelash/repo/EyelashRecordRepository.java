package com.example.membersystem.eyelash.repo;

import com.example.membersystem.eyelash.entity.EyelashRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 睫毛记录数据访问接口
 */
@Repository
public interface EyelashRecordRepository extends JpaRepository<EyelashRecord, Long> {

    /**
     * 根据手机号查找记录
     * 
     * @param phone 手机号
     * @return 睫毛记录列表
     */
    List<EyelashRecord> findByPhoneOrderByRecordDateDesc(String phone);

    /**
     * 根据姓氏查找记录
     * 
     * @param lastName 姓氏
     * @return 睫毛记录列表
     */
    List<EyelashRecord> findByLastNameOrderByRecordDateDesc(String lastName);

    /**
     * 根据手机号和姓氏查找记录
     * 
     * @param phone 手机号
     * @param lastName 姓氏
     * @return 睫毛记录列表
     */
    List<EyelashRecord> findByPhoneAndLastNameOrderByRecordDateDesc(String phone, String lastName);

    /**
     * 根据日期范围查找记录
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 睫毛记录列表
     */
    @Query("SELECT er FROM EyelashRecord er WHERE er.recordDate BETWEEN :startDate AND :endDate ORDER BY er.recordDate DESC")
    List<EyelashRecord> findByDateRangeOrderByRecordDateDesc(@Param("startDate") LocalDate startDate, 
                                                           @Param("endDate") LocalDate endDate);

    /**
     * 根据手机号和日期范围查找记录
     * 
     * @param phone 手机号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 睫毛记录列表
     */
    @Query("SELECT er FROM EyelashRecord er WHERE er.phone = :phone AND er.recordDate BETWEEN :startDate AND :endDate ORDER BY er.recordDate DESC")
    List<EyelashRecord> findByPhoneAndDateRangeOrderByRecordDateDesc(@Param("phone") String phone,
                                                                       @Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);

    /**
     * 根据姓氏和日期范围查找记录
     * 
     * @param lastName 姓氏
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 睫毛记录列表
     */
    @Query("SELECT er FROM EyelashRecord er WHERE er.lastName = :lastName AND er.recordDate BETWEEN :startDate AND :endDate ORDER BY er.recordDate DESC")
    List<EyelashRecord> findByLastNameAndDateRangeOrderByRecordDateDesc(@Param("lastName") String lastName,
                                                                         @Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);

    /**
     * 根据手机号、姓氏和日期范围查找记录
     * 
     * @param phone 手机号
     * @param lastName 姓氏
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 睫毛记录列表
     */
    @Query("SELECT er FROM EyelashRecord er WHERE er.phone = :phone AND er.lastName = :lastName AND er.recordDate BETWEEN :startDate AND :endDate ORDER BY er.recordDate DESC")
    List<EyelashRecord> findByPhoneAndLastNameAndDateRangeOrderByRecordDateDesc(@Param("phone") String phone,
                                                                               @Param("lastName") String lastName,
                                                                               @Param("startDate") LocalDate startDate,
                                                                               @Param("endDate") LocalDate endDate);

    /**
     * 根据款式查找记录
     * 
     * @param style 款式
     * @return 睫毛记录列表
     */
    List<EyelashRecord> findByStyleOrderByRecordDateDesc(String style);

    /**
     * 查找最近的记录
     * 
     * @return 睫毛记录列表
     */
    @Query("SELECT er FROM EyelashRecord er ORDER BY er.recordDate DESC, er.createdAt DESC")
    List<EyelashRecord> findRecentRecords();
}
