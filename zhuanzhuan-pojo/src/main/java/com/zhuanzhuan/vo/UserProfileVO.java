package com.zhuanzhuan.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    private Long id;
    private String studentNo;
    private String name;
    private String phone;
    private String avatar;
    private Integer role;
    private Integer status;
    private String campus;
    private String intro;
    private BigDecimal scoreAvg;
    private Integer reviewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
