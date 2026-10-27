package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationProgress;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.DecorationProgressMapper;
import com.example.decoratebackservice.mapper.UserAccountMapper;
import com.example.decoratebackservice.vo.OrderProgressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 装修进度跟踪业务层
 */
@Service
public class DecorationProgressService {

    @Autowired
    private DecorationProgressMapper decorationProgressMapper;

    /**
     * 按用户名反查 sys_user.id，用于确定当前登录用户
     */
    @Autowired
    private UserAccountMapper userAccountMapper;

    /** 订单状态：待支付 */
    private static final int ORDER_STATUS_UNPAID = 0;
    /** 订单状态：已支付 */
    private static final int ORDER_STATUS_PAID = 1;
    /** 订单状态：已完成 */
    private static final int ORDER_STATUS_FINISHED = 2;
    /** 订单状态：已退款 */
    private static final int ORDER_STATUS_REFUNDED = 3;

    public int insert(DecorationProgress progress) {
        return decorationProgressMapper.insert(progress);
    }

    public DecorationProgress findById(int id) {
        return decorationProgressMapper.findById(id);
    }

    public List<DecorationProgress> findAll() {
        return decorationProgressMapper.findAll();
    }

    public int update(DecorationProgress progress) {
        return decorationProgressMapper.update(progress);
    }

    public int delete(int id) {
        return decorationProgressMapper.delete(id);
    }

    /**
     * 查询某个施工人员自己上报的进度记录
     * <p>
     * 与 findMyProgress 的区别：那个是「客户按订单看进度」，
     * 这个服务于「施工人员回看自己提交了什么」，过滤条件不同。
     *
     * @param report_user_id 上报人 sys_user.id
     * @return 该人上报的记录，最新在前
     */
    public List<DecorationProgress> findByReportUserId(int report_user_id) {
        return decorationProgressMapper.findByReportUserId(report_user_id);
    }

    /**
     * 查询「当前登录用户」的装修进度
     * <p>
     * 可见性规则：订单状态为「已支付(1)」或「已完成(2)」时才可查看装修进度；
     * 待支付(0)、已退款(3) 一律不返回任何施工信息，只返回不可查看的原因。
     *
     * @param username 当前登录用户名（取自 SecurityContext）
     * @return 该用户全部订单 + 可查看的进度；用户不存在时返回空列表
     */
    public List<OrderProgressVO> findMyProgress(String username) {
        List<OrderProgressVO> result = new ArrayList<>();
        if (username == null || username.isBlank()) {
            return result;
        }
        // 前台用户登录时 principal 只有用户名，这里反查 sys_user.id
        List<User> users = userAccountMapper.findByUsername(username.trim());
        if (users.isEmpty()) {
            return result;
        }
        int userId = users.get(0).getId();

        for (OrderProgressVO vo : decorationProgressMapper.findOrderProgressByUserId(userId)) {
            boolean canView = isProgressVisible(vo.getOrder_status());
            vo.setCan_view(canView ? 1 : 0);

            if (!canView) {
                // 不可查看时清空所有施工信息，防止绕过前端直接读接口拿到内容
                vo.setProgress_id(null);
                vo.setProgress_status(null);
                vo.setStatus_desc(null);
                vo.setProgress_images(null);
                vo.setReport_time(null);
                if (vo.getOrder_status() == ORDER_STATUS_REFUNDED) {
                    vo.setMessage("该订单已退款，无法查看装修进度");
                } else if (vo.getOrder_status() == ORDER_STATUS_UNPAID) {
                    vo.setMessage("该订单待支付，支付成功后才可查看装修进度");
                } else {
                    vo.setMessage("当前订单状态不支持查看装修进度");
                }
            } else if (vo.getProgress_id() == null) {
                vo.setMessage("订单已支付，等待施工方上报进度");
            } else {
                vo.setMessage(null);
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 装修进度可见性：仅「已支付(1)」和「已完成(2)」的订单可查看
     *
     * @param order_status 订单状态
     * @return true 可查看
     */
    public static boolean isProgressVisible(int order_status) {
        return order_status == ORDER_STATUS_PAID || order_status == ORDER_STATUS_FINISHED;
    }
}
