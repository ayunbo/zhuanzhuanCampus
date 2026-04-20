package com.zhuanzhuan.platform.audit.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminReportPageQueryDTO;
import com.zhuanzhuan.entity.Report;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.ReportDetailVO;
import com.zhuanzhuan.vo.ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 举报数据访问层。
 */
@Mapper
public interface ReportMapper {

    /**
     * 插入举报记录。
     *
     * @param report 举报实体
     */
    @AutoFill(OperationType.INSERT)
    void insert(Report report);

    /**
     * 根据 ID 查询举报记录。
     *
     * @param id 举报 ID
     * @return 举报实体
     */
    @Select("SELECT * FROM report WHERE id = #{id}")
    Report selectById(Long id);

    /**
     * 分页查询举报列表（动态 SQL 在 XML 中实现）。
     *
     * @param dto 查询条件
     * @return 举报列表
     */
    List<ReportVO> pageQuery(AdminReportPageQueryDTO dto);

    /**
     * 查询举报详情（关联举报人和处理管理员名称）。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    ReportDetailVO selectDetailById(Long id);

    /**
     * 更新举报处理结果。
     *
     * @param report 举报实体（含处理信息）
     */
    @AutoFill(OperationType.UPDATE)
    void updateHandleResult(Report report);
}
