package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.mapper.DesignServiceOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 设计服务单业务层
 * <p>
 * 状态机（与 {@code 6_design_flow.sql} 保持一致）：
 * <pre>
 * 0 待设计师接单 → 1 待量房 → 2 已量房(待平面图) → 3 平面图待确认
 * → 4 平面图已确认(待定金) → 5 已付定金(待效果图) → 6 效果图已出(待报价)
 * → 7 已报价(待业主确认) → 8 报价已确认(待签合同) → 9 已签合同(待开工)
 * → 10 施工中 → 11 已完工(待尾款) → 12 已完成 ；13 已取消
 * </pre>
 * 所有推进都走 Mapper 的条件 UPDATE，返回 0 表示状态已被人改动。
 */
@Service
public class DesignServiceOrderService {

    public static final int STATUS_WAIT_ACCEPT = 0;
    public static final int STATUS_WAIT_MEASURE = 1;
    public static final int STATUS_MEASURED = 2;
    public static final int STATUS_PLAN_DRAWING = 3;
    public static final int STATUS_PLAN_CONFIRMED = 4;
    public static final int STATUS_DEPOSIT_PAID = 5;
    public static final int STATUS_EFFECT_DRAWING = 6;
    public static final int STATUS_QUOTED = 7;
    public static final int STATUS_QUOTE_CONFIRMED = 8;
    public static final int STATUS_CONTRACTED = 9;
    public static final int STATUS_BUILDING = 10;
    public static final int STATUS_WAIT_FINAL = 11;
    public static final int STATUS_FINISHED = 12;
    public static final int STATUS_CANCELED = 13;

    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private DesignServiceOrderMapper designServiceOrderMapper;

    /**
     * 业主提交设计需求（初始为待接单）
     */
    public int create(DesignServiceOrder order, int userId) {
        if (order.getDesigner_id() <= 0) {
            throw new IllegalArgumentException("请选择设计师");
        }
        if (order.getDecoration_mode() < 1 || order.getDecoration_mode() > 3) {
            throw new IllegalArgumentException("装修方式只能是 1硬装 2软装 3整装");
        }
        order.setUser_id(userId);
        order.setStatus(STATUS_WAIT_ACCEPT);
        order.setIs_deleted(0);
        order.setDeposit_amount(BigDecimal.ZERO);
        order.setService_no("DS" + LocalDateTime.now().format(NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000));
        return designServiceOrderMapper.insert(order);
    }

    public DesignServiceOrder findById(int id) {
        return designServiceOrderMapper.findById(id);
    }

    public List<DesignServiceOrder> findByUser(int userId) {
        return designServiceOrderMapper.findByUser(userId);
    }

    public List<DesignServiceOrder> findByDesigner(int designerId) {
        return designServiceOrderMapper.findByDesigner(designerId);
    }

    public List<DesignServiceOrder> findByManager(int managerId) {
        return designServiceOrderMapper.findByManager(managerId);
    }

    public List<DesignServiceOrder> findWaitContract() {
        return designServiceOrderMapper.findWaitContract();
    }

    /**
     * 设计师接单：待接单 → 待量房
     */
    public int accept(int id, int designerId) {
        DesignServiceOrder order = requireOrder(id);
        if (order.getDesigner_id() != designerId) {
            throw new IllegalArgumentException("只能承接指派给自己的设计需求");
        }
        return check(designServiceOrderMapper.advance(id, STATUS_WAIT_ACCEPT, STATUS_WAIT_MEASURE),
                "该需求已被处理，请刷新后重试");
    }

    /**
     * 设计师完成免费量房：待量房 → 已量房
     */
    public int measure(int id, int designerId) {
        requireDesigner(id, designerId);
        return check(designServiceOrderMapper.markMeasure(id, LocalDateTime.now()),
                "当前状态不能登记量房，请确认是否已接单");
    }

    /**
     * 设计师提交平面设计图，同时给出定金金额：已量房 → 平面图待确认
     */
    public int submitPlanDrawing(int id, int designerId, String drawings, BigDecimal depositAmount) {
        requireDesigner(id, designerId);
        if (drawings == null || drawings.isBlank()) {
            throw new IllegalArgumentException("请上传平面设计图");
        }
        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("请填写定金金额");
        }
        return check(designServiceOrderMapper.markPlanDrawing(id, LocalDateTime.now(), drawings, depositAmount),
                "当前状态不能提交平面图，请确认是否已量房");
    }

    /**
     * 业主确认平面图：平面图待确认 → 待付定金
     */
    public int confirmPlan(int id, int userId) {
        requireOwner(id, userId);
        return check(designServiceOrderMapper.markPlanConfirm(id, LocalDateTime.now()),
                "当前状态不能确认平面图");
    }

    /**
     * 业主支付定金：待付定金 → 已付定金
     */
    public int payDeposit(int id, int userId) {
        DesignServiceOrder order = requireOwner(id, userId);
        if (order.getDeposit_amount() == null || order.getDeposit_amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("尚未确定定金金额");
        }
        return check(designServiceOrderMapper.markDeposit(id, order.getDeposit_amount(), LocalDateTime.now()),
                "当前状态不能支付定金");
    }

    /**
     * 设计师提交效果图：已付定金 → 效果图已出
     */
    public int submitEffectDrawing(int id, int designerId, String drawings) {
        requireDesigner(id, designerId);
        if (drawings == null || drawings.isBlank()) {
            throw new IllegalArgumentException("请上传效果图");
        }
        return check(designServiceOrderMapper.markEffectDrawing(id, LocalDateTime.now(), drawings),
                "当前状态不能提交效果图，请确认定金是否已支付");
    }

    /**
     * 业主确认报价：已报价 → 待签合同
     */
    public int confirmQuote(int id, int userId) {
        requireOwner(id, userId);
        return check(designServiceOrderMapper.advance(id, STATUS_QUOTED, STATUS_QUOTE_CONFIRMED),
                "当前状态不能确认报价");
    }

    public int cancel(int id, int userId, String reason) {
        requireOwner(id, userId);
        return check(designServiceOrderMapper.markCancel(id, reason == null ? "业主取消" : reason),
                "合同已签订，无法取消，请联系项目经理");
    }

    /**
     * 合同签订后回写：待签合同 → 已签合同
     */
    public int markContracted(int id, int contractId, int managerId, java.time.LocalDate startDate) {
        return check(designServiceOrderMapper.markContract(id, contractId, managerId, LocalDateTime.now(), startDate),
                "当前状态不能签订合同，请确认业主是否已确认报价");
    }

    /**
     * 首个施工阶段开工：已签合同 → 施工中
     */
    public int markBuilding(int id) {
        designServiceOrderMapper.markStart(id);
        return 1;
    }

    /**
     * 全部阶段验收通过：施工中 → 待付尾款
     */
    public int markWaitFinal(int id) {
        designServiceOrderMapper.markWaitFinal(id);
        return 1;
    }

    /**
     * 尾款结清：待付尾款 → 已完成
     */
    public int markFinished(int id) {
        designServiceOrderMapper.markFinish(id, LocalDateTime.now());
        return 1;
    }

    public int bindManager(int id, int managerId) {
        return designServiceOrderMapper.bindManager(id, managerId);
    }

    /**
     * 校验服务单存在且未删除
     */
    public DesignServiceOrder requireOrder(int id) {
        DesignServiceOrder order = designServiceOrderMapper.findById(id);
        if (order == null || order.getIs_deleted() != 0) {
            throw new IllegalArgumentException("设计服务单不存在");
        }
        return order;
    }

    private DesignServiceOrder requireDesigner(int id, int designerId) {
        DesignServiceOrder order = requireOrder(id);
        if (order.getDesigner_id() != designerId) {
            throw new IllegalArgumentException("无权操作他人的设计服务单");
        }
        return order;
    }

    private DesignServiceOrder requireOwner(int id, int userId) {
        DesignServiceOrder order = requireOrder(id);
        if (order.getUser_id() != userId) {
            throw new IllegalArgumentException("无权操作他人的设计服务单");
        }
        return order;
    }

    private int check(int rows, String message) {
        if (rows == 0) {
            throw new IllegalArgumentException(message);
        }
        return rows;
    }
}
