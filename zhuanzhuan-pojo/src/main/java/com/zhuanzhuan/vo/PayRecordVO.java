package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PayRecordVO implements Serializable {

    private Long id;
    private Long payId;
    private Long orderId;
    private String recordNo;
    private String content;
    private Integer status;
    private String channelResponse;
    private LocalDateTime createTime;
}