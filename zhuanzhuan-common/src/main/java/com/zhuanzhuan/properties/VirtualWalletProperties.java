package com.zhuanzhuan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "zhuanzhuan.virtual-wallet")
@Data
public class VirtualWalletProperties {

    private String scheme = "virtualwallet://pay";

    private String appId = "zhuanzhuan-campus";

    private String callbackUrl = "http://localhost:8080/wallet/pay/callback";

    private String returnUrl = "zhuanzhuan://pay/result";

    private String signSecret = "virtual-wallet-secret";
}
