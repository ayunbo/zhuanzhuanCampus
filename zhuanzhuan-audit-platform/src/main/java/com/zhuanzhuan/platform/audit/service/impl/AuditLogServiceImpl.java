package com.zhuanzhuan.platform.audit.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.dto.AdminAuditLogPageQueryDTO;
import com.zhuanzhuan.platform.audit.mapper.AuditLogMapper;
import com.zhuanzhuan.platform.audit.service.AuditLogService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.AuditLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核流水业务实现。
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    /**
     * 管理员分页查询审核流水。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(AdminAuditLogPageQueryDTO dto) {
        // 1、启动分页插件
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 2、执行查询
        List<AuditLogVO> records = auditLogMapper.pageQuery(dto);
        Page<AuditLogVO> pageInfo = (Page<AuditLogVO>) records;

        // 3、封装分页结果返回
        return new PageResult(pageInfo.getTotal(), records);
    }
}
