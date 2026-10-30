package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DesignQuote;
import com.example.decoratebackservice.entity.DesignQuoteItem;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.mapper.DesignQuoteItemMapper;
import com.example.decoratebackservice.mapper.DesignQuoteMapper;
import com.example.decoratebackservice.mapper.DesignServiceOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 报价单业务层
 * <p>
 * 报价只能在「效果图已出」之后提交；提交后设计服务单进入「待业主确认」，
 * 业主确认后报价单置为已确认，服务单进入「待签合同」，合同金额取报价总额。
 */
@Service
public class DesignQuoteService {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_WAIT_CONFIRM = 1;
    public static final int STATUS_CONFIRMED = 2;
    public static final int STATUS_INVALID = 3;

    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private DesignQuoteMapper designQuoteMapper;

    @Autowired
    private DesignQuoteItemMapper designQuoteItemMapper;

    @Autowired
    private DesignServiceOrderMapper designServiceOrderMapper;

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    /**
     * 设计师提交报价（一次提交会作废旧报价，保证只有一份生效）
     */
    public DesignQuote submit(int serviceId, int designerId, List<DesignQuoteItem> items, String remark) {
        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        if (order.getDesigner_id() != designerId) {
            throw new IllegalArgumentException("无权为该设计服务单报价");
        }
        if (order.getStatus() != DesignServiceOrderService.STATUS_EFFECT_DRAWING) {
            throw new IllegalArgumentException("效果图出图后才能报价");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("报价明细不能为空");
        }

        BigDecimal hard = BigDecimal.ZERO;
        BigDecimal soft = BigDecimal.ZERO;
        for (DesignQuoteItem item : items) {
            if (item.getItem_name() == null || item.getItem_name().isBlank()) {
                throw new IllegalArgumentException("报价明细的项目名称不能为空");
            }
            if (item.getItem_type() != 1 && item.getItem_type() != 2) {
                throw new IllegalArgumentException("明细类型只能是 1硬装 或 2软装");
            }
            BigDecimal quantity = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            BigDecimal unitPrice = item.getUnit_price() == null ? BigDecimal.ZERO : item.getUnit_price();
            BigDecimal amount = item.getAmount() == null ? quantity.multiply(unitPrice) : item.getAmount();
            item.setAmount(amount);
            if (item.getItem_type() == 1) {
                hard = hard.add(amount);
            } else {
                soft = soft.add(amount);
            }
        }

        DesignQuote quote = new DesignQuote();
        quote.setQuote_no("Q" + LocalDateTime.now().format(NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000));
        quote.setService_id(serviceId);
        quote.setUser_id(order.getUser_id());
        quote.setDesigner_id(designerId);
        quote.setHard_amount(hard);
        quote.setSoft_amount(soft);
        quote.setTotal_amount(hard.add(soft));
        quote.setStatus(STATUS_WAIT_CONFIRM);
        quote.setRemark(remark);
        quote.setIs_deleted(0);
        designQuoteMapper.insert(quote);

        for (int i = 0; i < items.size(); i++) {
            DesignQuoteItem item = items.get(i);
            item.setQuote_id(quote.getId());
            item.setSort(i);
            item.setIs_deleted(0);
            designQuoteItemMapper.insert(item);
        }

        designQuoteMapper.invalidateOthers(serviceId, quote.getId());
        int rows = designServiceOrderMapper.markQuote(serviceId, quote.getId(), LocalDateTime.now());
        if (rows == 0) {
            throw new IllegalArgumentException("当前状态不能提交报价");
        }
        return quote;
    }

    /**
     * 量房阶段报价：设计师在「已量房」时录入硬装 / 软装金额，总额自动汇总。
     * <p>
     * 与 {@link #submit} 的区别：不要求报价明细、不推进设计服务单状态
     * （订单仍停在「已量房」，待设计师提交平面图后进入「平面图待确认」），
     * 报价单直接置为「待业主确认」，供业主在装修进度中查看并据此支付定金。
     */
    public DesignQuote submitMeasureQuote(int serviceId, int designerId,
                                          BigDecimal hardAmount, BigDecimal softAmount, String remark) {
        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        if (order.getDesigner_id() != designerId) {
            throw new IllegalArgumentException("无权为该设计服务单报价");
        }
        if (order.getStatus() != DesignServiceOrderService.STATUS_MEASURED) {
            throw new IllegalArgumentException("仅「已量房」状态可以录入报价");
        }
        BigDecimal hard = hardAmount == null ? BigDecimal.ZERO : hardAmount;
        BigDecimal soft = softAmount == null ? BigDecimal.ZERO : softAmount;
        if (hard.signum() < 0 || soft.signum() < 0) {
            throw new IllegalArgumentException("硬装 / 软装金额不能为负数");
        }
        BigDecimal total = hard.add(soft);
        if (total.signum() <= 0) {
            throw new IllegalArgumentException("硬装与软装金额不能同时为 0");
        }

        DesignQuote quote = new DesignQuote();
        quote.setQuote_no("Q" + LocalDateTime.now().format(NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000));
        quote.setService_id(serviceId);
        quote.setUser_id(order.getUser_id());
        quote.setDesigner_id(designerId);
        quote.setHard_amount(hard);
        quote.setSoft_amount(soft);
        quote.setTotal_amount(total);
        quote.setStatus(STATUS_WAIT_CONFIRM);
        quote.setRemark(remark);
        quote.setIs_deleted(0);
        designQuoteMapper.insert(quote);
        // 同一服务单保留最新一份生效报价
        designQuoteMapper.invalidateOthers(serviceId, quote.getId());
        return quote;
    }

    public DesignQuote findById(int id) {
        return designQuoteMapper.findById(id);
    }

    public List<DesignQuote> findByService(int serviceId) {
        return designQuoteMapper.findByService(serviceId);
    }

    public List<DesignQuoteItem> findItems(int quoteId) {
        List<DesignQuoteItem> items = designQuoteItemMapper.findByQuote(quoteId);
        return items == null ? Collections.emptyList() : items;
    }

    /**
     * 生效报价（待业主确认或已确认）
     */
    public DesignQuote findActive(int serviceId) {
        return designQuoteMapper.findActiveByService(serviceId);
    }

    /**
     * 业主确认报价：报价单已确认 + 设计服务单进入待签合同
     */
    public DesignQuote confirm(int quoteId, int userId) {
        DesignQuote quote = designQuoteMapper.findById(quoteId);
        if (quote == null || quote.getIs_deleted() != 0) {
            throw new IllegalArgumentException("报价单不存在");
        }
        if (quote.getUser_id() != userId) {
            throw new IllegalArgumentException("无权确认他人的报价单");
        }
        if (quote.getStatus() != STATUS_WAIT_CONFIRM) {
            throw new IllegalArgumentException("当前报价单状态不能确认");
        }
        designQuoteMapper.updateStatus(quoteId, STATUS_CONFIRMED, LocalDateTime.now());
        designServiceOrderService.confirmQuote(quote.getService_id(), userId);
        return designQuoteMapper.findById(quoteId);
    }
}
