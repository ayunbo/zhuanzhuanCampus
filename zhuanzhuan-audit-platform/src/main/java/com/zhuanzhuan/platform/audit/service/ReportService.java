package com.zhuanzhuan.platform.audit.service;

import com.zhuanzhuan.dto.AdminReportPageQueryDTO;
import com.zhuanzhuan.dto.ReportHandleDTO;
import com.zhuanzhuan.dto.ReportSubmitDTO;
import com.zhuanzhuan.dto.UserReportPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.ReportDetailVO;

/**
 * 举报业务接口。
 */
public interface ReportService {

    /**
     * 用户提交举报。
     *
     * @param dto 举报信息
     */
    void submitReport(ReportSubmitDTO dto);

    /**
     * 当前登录用户分页查询本人提交的举报记录。
     *
     * @param dto 查询条件
     * @return 本人的举报分页结果
     */
    PageResult pageCurrentUserReports(UserReportPageQueryDTO dto);

    /**
     * 当前登录用户查看本人提交的举报详情及处理结果。
     *
     * @param id 举报 ID
     * @return 本人的举报详情
     */
    ReportDetailVO getCurrentUserReportDetail(Long id);

    /**
     * 管理员分页查询举报列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(AdminReportPageQueryDTO dto);

    /**
     * 管理员查询举报详情。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    ReportDetailVO getDetail(Long id);

    /**
     * 管理员处理举报。
     *
     * @param id 举报 ID
     * @param dto 处理信息
     */
    void handleReport(Long id, ReportHandleDTO dto);

    /**
     * 管理员忽略举报。
     *
     * @param id 举报 ID
     */
    void ignoreReport(Long id);
}
