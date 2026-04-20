package com.zhuanzhuan.platform.trade.controller.admin;

import com.zhuanzhuan.dto.AdminOrderPageQueryDTO;
import com.zhuanzhuan.dto.AdminOrderStatusDTO;
import com.zhuanzhuan.dto.AdminOrderUpdateDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.trade.service.AdminOrderService;
import com.zhuanzhuan.vo.AdminOrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping("/page")
    public Result<PageResult> page(AdminOrderPageQueryDTO dto) {
        return Result.success(adminOrderService.pageQuery(dto));
    }

    @GetMapping("/detail/{id}")
    public Result<AdminOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminOrderService.detail(id));
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody AdminOrderUpdateDTO dto) {
        adminOrderService.updateOrder(dto);
        return Result.success("修改成功");
    }

    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody AdminOrderStatusDTO dto) {
        adminOrderService.updateStatus(dto);
        return Result.success("状态修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        adminOrderService.deleteOrder(id);
        return Result.success("删除成功");
    }
}