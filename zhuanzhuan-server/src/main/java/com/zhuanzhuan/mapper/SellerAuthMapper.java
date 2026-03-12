package com.zhuanzhuan.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminSellerAuthPageQueryDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.SellerAuthResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卖家认证表数据访问接口。
 */
@Mapper
public interface SellerAuthMapper {

    /**
     * 根据认证 ID 查询认证记录。
     *
     * @param id 认证 ID
     * @return 认证实体
     */
    SellerAuth getById(Long id);

    /**
     * 查询指定用户最近一次卖家认证申请。
     *
     * @param userId 用户 ID
     * @return 最近一次申请
     */
    SellerAuth getLatestByUserId(Long userId);

    /**
     * 管理员分页条件查询卖家认证列表。
     *
     * @param pageQueryDTO 分页和筛选条件
     * @return 查询结果
     */
    List<SellerAuthResultVO> pageQuery(AdminSellerAuthPageQueryDTO pageQueryDTO);

    /**
     * 提交卖家认证申请。
     *
     * @param sellerAuth 认证参数
     * @return 影响行数
     */
    @AutoFill(OperationType.INSERT)
    int insert(SellerAuth sellerAuth);

    /**
     * 审核卖家认证申请。
     *
     * @param sellerAuth 审核参数
     * @return 影响行数
     */
    @AutoFill(OperationType.UPDATE)
    int updateAuditById(SellerAuth sellerAuth);

    /**
     * 根据用户 ID 批量更新认证状态（预留扩展能力）。
     *
     * @param userId 用户 ID
     * @param status 认证状态
     * @return 影响行数
     */
    int updateStatusByUserId(@Param("userId") Long userId, @Param("status") Integer status);
}
