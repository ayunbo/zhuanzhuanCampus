package com.zhuanzhuan;

import com.zhuanzhuan.mapper.TestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ZhuanzhuanServerApplicationTests {

    @Autowired
    private TestMapper employeeMapper;

    @Test
    void testDbConnection() {
        System.out.println("开始测试数据库连接...");

        // 假设你数据库的 employee 表里有一个 id 为 1 的数据。
        // 如果没有 1L，你可以换成数据库里真实存在的 ID。
        Object employee = employeeMapper.getById(133L);

        System.out.println("数据库连接成功！查询结果：" + employee);
    }



}
