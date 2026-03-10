package com.zhuanzhuan.controller;

import com.zhuanzhuan.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Employee APIs")
@RestController
@RequestMapping("/employee")
public class UserController {

    @Operation(summary = "Get employee info by id")
    @GetMapping("/{id}")
    public Result getById(@Parameter(description = "Employee id", required = true) @PathVariable Long id) {
        System.out.println("Request employee id: " + id);
        return Result.success();
    }
}
