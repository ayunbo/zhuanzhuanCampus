package com.zhuanzhuan.platform.audit.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminAuditLogPageQueryDTO;
import com.zhuanzhuan.entity.AuditLog;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.AuditLogVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 审核流水数据访问层。
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审核流水记录。
     *
     * @param auditLog 审核流水实体
     */
    @AutoFill(OperationType.INSERT)
    void insert(AuditLog auditLog);

    /**
     * 分页查询审核流水列表（动态 SQL 在 XML 中实现）。
     *
     * @param dto 查询条件
     * @return 审核流水列表
     */
    List<AuditLogVO> pageQuery(AdminAuditLogPageQueryDTO dto);

    /**
     * 根据日志 ID 查询单条日志视图信息。
     *
     * @param id 日志 ID
     * @return 日志视图
     */
    AuditLogVO getById(Long id);
}
