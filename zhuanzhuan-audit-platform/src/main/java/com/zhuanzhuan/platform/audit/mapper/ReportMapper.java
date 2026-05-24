package com.zhuanzhuan.platform.audit.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminReportPageQueryDTO;
import com.zhuanzhuan.dto.UserReportPageQueryDTO;
import com.zhuanzhuan.entity.Report;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.ReportDetailVO;
import com.zhuanzhuan.vo.ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     * 用户端分页查询本人提交的举报记录。
     *
     * @param reportUserId 当前登录用户 ID
     * @param dto 查询条件
     * @return 本人举报列表
     */
    List<ReportVO> pageQueryByUserId(@Param("reportUserId") Long reportUserId,
                                     @Param("dto") UserReportPageQueryDTO dto);

    /**
     * 查询举报详情（关联举报人和处理管理员名称）。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    ReportDetailVO selectDetailById(Long id);

    /**
     * 根据举报 ID 和举报人 ID 查询详情。
     * 用于用户端“我的举报详情”，通过 reportUserId 限制数据归属，防止越权查看他人举报。
     *
     * @param id 举报 ID
     * @param reportUserId 举报人 ID
     * @return 举报详情
     */
    ReportDetailVO selectDetailByIdAndReportUserId(@Param("id") Long id,
                                                   @Param("reportUserId") Long reportUserId);

    /**
     * 更新举报处理结果。
     *
     * @param report 举报实体（含处理信息）
     */
    @AutoFill(OperationType.UPDATE)
    int updateHandleResult(Report report);

    /**
     * 统计当前用户针对同一对象仍处于待处理状态的举报数量。
     * 防止用户在管理员处理前反复提交相同举报，减少运营侧重复工单。
     */
    @Select("""
            SELECT count(1)
            FROM report
            WHERE report_user_id = #{reportUserId}
              AND target_type = #{targetType}
              AND target_id = #{targetId}
              AND status = 0
            """)
    int countPendingByReporterAndTarget(@Param("reportUserId") Long reportUserId,
                                        @Param("targetType") Integer targetType,
                                        @Param("targetId") Long targetId);

    /**
     * 判断商品举报对象是否存在，并返回商品卖家 ID。
     * 返回 null 表示商品不存在。
     */
    @Select("SELECT seller_id FROM goods WHERE id = #{goodsId} LIMIT 1")
    Long selectGoodsSellerId(Long goodsId);

    /**
     * 判断用户举报对象是否存在。
     */
    @Select("SELECT count(1) FROM `user` WHERE id = #{userId}")
    int countUserById(Long userId);

    /**
     * 判断聊天消息是否存在，且当前用户是该消息的发送方或接收方。
     * 消息举报必须限制在与当前用户有关的消息上，避免通过猜测消息 ID 越权举报。
     */
    @Select("""
            SELECT count(1)
            FROM chatmessage
            WHERE id = #{messageId}
              AND (sender_id = #{currentUserId} OR receiver_id = #{currentUserId})
            """)
    int countMessageVisibleToUser(@Param("messageId") Long messageId,
                                  @Param("currentUserId") Long currentUserId);
}
