package com.example.decoratebackservice.service;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.example.decoratebackservice.config.AlipayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 支付宝网关封装：电脑网站支付下单、回调验签、交易查询
 * <p>
 * 只在 alipay.mock=false 且配置齐全时才会真正访问支付宝，
 * AlipayClient 采用懒加载，避免开发环境缺少密钥时启动失败。
 */
@Service
public class AlipayGatewayService {

    private static final Logger log = LoggerFactory.getLogger(AlipayGatewayService.class);

    /** 电脑网站支付产品码 */
    private static final String PAGE_PAY_PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";

    /** 手机网站支付产品码 */
    private static final String WAP_PAY_PRODUCT_CODE = "QUICK_WAP_WAY";

    @Autowired
    private AlipayProperties properties;

    private volatile AlipayClient client;

    /**
     * 创建支付订单，返回可直接提交到支付宝的自动跳转表单 HTML
     * <p>
     * 具体走电脑网站支付还是手机网站支付由 alipay.pay-channel 决定，
     * 沙箱环境只支持后者，正式环境用前者。
     */
    public String pagePayForm(String outTradeNo, BigDecimal amount, String subject) throws AlipayApiException {
        return properties.isWapChannel()
                ? wapPayForm(outTradeNo, amount, subject)
                : pagePayForm0(outTradeNo, amount, subject);
    }

    /** 电脑网站支付：正式环境使用 */
    private String pagePayForm0(String outTradeNo, BigDecimal amount, String subject) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(properties.getNotifyUrl());
        request.setReturnUrl(properties.getReturnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        // 支付宝金额单位是「元」，保留两位小数
        bizContent.put("total_amount", amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        bizContent.put("subject", subject);
        bizContent.put("product_code", PAGE_PAY_PRODUCT_CODE);
        request.setBizContent(bizContent.toJSONString());

        // 第二个参数传 POST，返回带自动提交 JS 的表单，前端插入 DOM 后即可跳转收银台
        return client().pageExecute(request, "POST").getBody();
    }

    /** 手机网站支付：沙箱环境使用（沙箱不提供电脑网站支付） */
    private String wapPayForm(String outTradeNo, BigDecimal amount, String subject) throws AlipayApiException {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(properties.getNotifyUrl());
        request.setReturnUrl(properties.getReturnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        bizContent.put("subject", subject);
        bizContent.put("product_code", WAP_PAY_PRODUCT_CODE);
        // 用户在收银台中途退出时跳回的地址，手机网站支付要求必须携带
        bizContent.put("quit_url", properties.getReturnUrl());
        request.setBizContent(bizContent.toJSONString());

        return client().pageExecute(request, "POST").getBody();
    }

    /**
     * 回调验签：支付宝公钥 + RSA2，验签失败说明请求不是支付宝发来的，必须拒绝
     */
    public boolean verify(Map<String, String> params) throws AlipayApiException {
        return AlipaySignature.rsaCheckV1(params, properties.getAlipayPublicKey(), "UTF-8", "RSA2");
    }

    /**
     * 主动查单，用于回调丢失时补偿
     *
     * @return 支付宝交易状态，如 TRADE_SUCCESS / WAIT_BUYER_PAY，查不到返回 null
     */
    public String queryTradeStatus(String outTradeNo) {
        AlipayTradeQueryResponse response = queryTrade(outTradeNo);
        if (response != null && response.isSuccess()) {
            return response.getTradeStatus();
        }
        if (response != null) {
            log.warn("查询支付宝交易失败：{}", response.getSubMsg());
        }
        return null;
    }

    /**
     * 主动查单，返回支付宝原始响应（含 trade_no / trade_status），用于回调丢失时补偿落库
     *
     * @return 查单响应，网络或验签异常返回 null
     */
    public AlipayTradeQueryResponse queryTrade(String outTradeNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", outTradeNo);
            request.setBizContent(bizContent.toJSONString());
            return client().execute(request);
        } catch (AlipayApiException e) {
            log.warn("查询支付宝交易异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 懒加载 AlipayClient：mock 模式下不会走到这里
     */
    private AlipayClient client() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new DefaultAlipayClient(
                            properties.getGateway(),
                            properties.getAppId(),
                            properties.getPrivateKey(),
                            "json",
                            "UTF-8",
                            properties.getAlipayPublicKey(),
                            "RSA2");
                }
            }
        }
        return client;
    }
}
