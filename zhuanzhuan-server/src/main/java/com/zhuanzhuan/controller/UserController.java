package com.zhuanzhuan.controller;

import com.zhuanzhuan.mapper.UserMapper;
import com.zhuanzhuan.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employee") // 定义接口的基础路径
public class UserController {

        // 测试接口：根据 ID 获取员工信息
        @GetMapping("/{id}")
        public Result getById(@PathVariable Long id) {
            System.out.println("前端发来请求啦！查询的ID是：" + id);
            return Result.success();
        }

}
