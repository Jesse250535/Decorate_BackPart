package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.AlipayProperties;
import com.example.decoratebackservice.entity.PaymentRecord;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.service.PaymentService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付接口：创建交易、支付结果查询、支付宝回调
 * <p>
 * /alipay/notify 与 /alipay/return 由支付宝服务器直接访问，不带登录态，
 * 已在 SecurityConfig 中放行，安全边界依赖验签（RSA2）。
 * /mock/** 仅在 alipay.mock=true 时可用，用于本地跑通全流程。
 */
@Tag(name = "支付接口")
@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private AlipayProperties properties;

    @Operation(summary = "为某个付款节点创建支付交易（返回收银台表单或模拟支付页地址）")
    @PostMapping("/create")
    public Result<Map<String, Object>> create(@RequestBody(required = false) Map<String, Object> body,
                                              Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        Object nodeId = body == null ? null : body.get("node_id");
        if (nodeId == null) {
            return Result.validateFailed("缺少参数：node_id");
        }
        int id;
        try {
            id = Integer.parseInt(String.valueOf(nodeId));
        } catch (NumberFormatException e) {
            return Result.validateFailed("node_id 格式不正确");
        }
        return Result.success("已创建支付", paymentService.create(id, userId));
    }

    @Operation(summary = "查询支付结果（前端支付完成后轮询）")
    @GetMapping("/status/{outTradeNo}")
    public Result<Map<String, Object>> status(@PathVariable String outTradeNo, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(paymentService.status(outTradeNo, userId));
    }

    @Operation(summary = "支付宝异步通知（由支付宝服务器调用，验签后落库）")
    @PostMapping(value = "/alipay/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String alipayNotify(@RequestParam Map<String, String> params) {
        return paymentService.handleNotify(params);
    }

    @Operation(summary = "支付宝同步跳回：验签后跳转到前端装修进度页")
    @GetMapping("/alipay/return")
    public void alipayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        response.sendRedirect(paymentService.handleReturn(params));
    }

    @Operation(summary = "本地模拟收银台（仅 alipay.mock=true 时可用）")
    @GetMapping(value = "/mock/{outTradeNo}", produces = "text/html;charset=UTF-8")
    public String mockCashier(@PathVariable String outTradeNo) {
        if (!properties.isMock()) {
            throw new IllegalArgumentException("当前为真实支付模式，模拟收银台已关闭");
        }
        return renderCashier(paymentService.requireRecord(outTradeNo));
    }

    @Operation(summary = "本地模拟收银台确认支付（仅 alipay.mock=true 时可用）")
    @PostMapping(value = "/mock/{outTradeNo}/confirm", produces = "text/html;charset=UTF-8")
    public String mockConfirm(@PathVariable String outTradeNo) {
        paymentService.mockConfirm(outTradeNo);
        return renderCashier(paymentService.requireRecord(outTradeNo));
    }

    /**
     * 模拟收银台页面：未支付时显示付款按钮，已支付显示成功提示
     */
    private String renderCashier(PaymentRecord record) {
        boolean paid = record.getStatus() == 1;
        BigDecimal amount = record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
        String body = paid
                ? "<p class=\"done\">支付成功，付款节点已更新</p>"
                  + "<a class=\"btn ghost\" href=\"" + properties.getFrontUrl() + "\">返回装修进度</a>"
                // action 必须用相对路径：收银台可能经 Vite 代理（/api/payment/mock/xx）
                // 或直接访问后端（/payment/mock/xx），绝对路径会打到前端 5173 上导致 404
                : "<form method=\"post\" action=\"" + record.getOut_trade_no() + "/confirm\">"
                  + "<button class=\"btn\" type=\"submit\">确认支付 ¥" + amount.toPlainString() + "</button>"
                  + "</form>"
                  + "<p class=\"tip\">这是本地模拟收银台，点击后走与真实回调相同的落库逻辑，不会产生任何真实扣款。</p>";
        return "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>模拟支付</title><style>"
                + "*{box-sizing:border-box}body{margin:0;min-height:100vh;display:flex;align-items:center;"
                + "justify-content:center;background:#0f172a;font-family:-apple-system,'Segoe UI',sans-serif;padding:24px}"
                + ".card{width:min(420px,100%);padding:28px;background:#ffffff;border-radius:16px;"
                + "box-shadow:0 24px 60px rgba(0,0,0,.35)}"
                + ".tag{margin:0 0 16px;font-size:12px;color:#0f766e;background:#ccfbf1;display:inline-block;"
                + "padding:4px 10px;border-radius:999px}"
                + "h1{margin:0;font-size:34px;letter-spacing:-.5px;color:#0f172a}"
                + ".subject{margin:8px 0 0;font-size:15px;color:#334155}"
                + ".meta{margin:6px 0 22px;font-size:12px;color:#94a3b8;word-break:break-all}"
                + ".btn{display:block;width:100%;padding:13px 18px;border:0;border-radius:12px;background:#0d9488;"
                + "color:#fff;font-size:15px;font-weight:600;text-align:center;text-decoration:none;cursor:pointer}"
                + ".btn:hover{background:#0f766e}.btn.ghost{background:#e2e8f0;color:#0f172a}"
                + ".tip{margin:14px 0 0;font-size:12px;line-height:1.7;color:#94a3b8}"
                + ".done{margin:0 0 16px;font-size:15px;color:#0d9488;font-weight:600}"
                + "</style></head><body><div class=\"card\">"
                + "<p class=\"tag\">开发环境 · 模拟收银台</p>"
                + "<h1>¥" + amount.toPlainString() + "</h1>"
                + "<p class=\"subject\">" + escape(record.getSubject()) + "</p>"
                + "<p class=\"meta\">订单号 " + record.getOut_trade_no() + "</p>"
                + body
                + "</div></body></html>";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByUsernameForStaff(authentication.getName());
        return user == null ? null : user.getId();
    }
}
