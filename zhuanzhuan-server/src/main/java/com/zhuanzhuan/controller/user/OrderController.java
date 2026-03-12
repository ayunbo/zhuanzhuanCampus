package com.zhuanzhuan.controller.user;

import com.zhuanzhuan.dto.OrderPageQueryDTO;
import com.zhuanzhuan.dto.OrderSubmitDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.OrderService;
import com.zhuanzhuan.vo.OrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<Long> submit(@RequestBody OrderSubmitDTO dto) {
        return Result.success(orderService.submit(dto));
    }

    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success("取消成功");
    }

    @PostMapping("/complete/{id}")
    public Result<String> complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success("完成成功");
    }

    @GetMapping("/page")
    public Result<PageResult> page(OrderPageQueryDTO dto) {
        return Result.success(orderService.pageQuery(dto));
    }

    @GetMapping("/detail/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }
}