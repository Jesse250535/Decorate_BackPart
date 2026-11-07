package com.example.decoratebackservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置（application.properties 中 alipay.* 前缀）
 * <p>
 * mock = true 时不调用支付宝网关，用本地模拟收银台跑通「创建交易 → 支付 → 回调 → 节点已付」全流程，
 * 方便没有商家资质 / 密钥时开发联调。拿到沙箱或正式 APPID 后把 mock 改为 false 即可切换到真实支付。
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /** 是否使用本地模拟支付（true：不走支付宝网关） */
    private boolean mock = true;

    /** 应用 APPID（沙箱或正式） */
    private String appId;

    /** 网关：沙箱 https://openapi-sandbox.dl.alipaydev.com/gateway.do，正式 https://openapi.alipay.com/gateway.do */
    private String gateway = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /** 应用私钥（RSA2，PKCS8，不含 PEM 头尾） */
    private String privateKey;

    /** 支付宝公钥（在开放平台上传应用公钥后生成，不是应用公钥本身） */
    private String alipayPublicKey;

    /** 异步通知地址，必须是公网可访问的地址，支付宝服务器会主动 POST 过来 */
    private String notifyUrl;

    /** 支付完成后支付宝跳回的地址（后端接口，验签后再跳到前端页面） */
    private String returnUrl;

    /** 支付完成后前端页面地址，例如 http://localhost:5173/renovation */
    private String frontUrl = "http://localhost:5173/renovation";

    /**
     * 支付渠道：page=电脑网站支付（alipay.trade.page.pay），wap=手机网站支付（alipay.trade.wap.pay）
     * <p>
     * 支付宝沙箱环境只提供手机网站支付接口，电脑网站支付会直接跳到错误页，
     * 因此沙箱联调时填 wap，正式环境再改回 page。
     */
    private String payChannel = "page";

    /**
     * 是否已经配置好真实支付所需的信息
     */
    /** 当前是否使用手机网站支付通道 */
    public boolean isWapChannel() {
        return "wap".equalsIgnoreCase(payChannel == null ? "" : payChannel.trim());
    }

    public boolean configured() {
        return !mock
                && notBlank(appId)
                && notBlank(privateKey)
                && notBlank(alipayPublicKey)
                && notBlank(notifyUrl);
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
