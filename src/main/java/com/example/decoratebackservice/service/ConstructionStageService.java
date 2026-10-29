package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.ConstructionStage;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.mapper.ConstructionStageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 施工阶段业务层
 * <p>
 * 负责：按装修方式生成阶段计划 → 开工 / 完工 / 业主验收 / 返工登记。
 * 「是否按时」在完工时判定：实际结束日期 ≤ 计划结束日期为按时，否则记为超期并累计超期天数。
 */
@Service
public class ConstructionStageService {

    public static final int STAGE_NOT_STARTED = 0;
    public static final int STAGE_DOING = 1;
    public static final int STAGE_DONE = 2;
    public static final int STAGE_ACCEPTED = 3;
    public static final int STAGE_REWORK = 4;

    public static final int ACCEPT_WAIT = 0;
    public static final int ACCEPT_PASS = 1;
    public static final int ACCEPT_REJECT = 2;

    public static final int ON_TIME_UNKNOWN = 0;
    public static final int ON_TIME_YES = 1;
    public static final int ON_TIME_DELAY = 2;

    @Autowired
    private ConstructionStageMapper constructionStageMapper;

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    /**
     * 阶段模板：编码、名称、硬/软装、计划工期(天)
     */
    private record Template(String code, String name, int type, int days) {
    }

    private static final List<Template> HARD_STAGES = List.of(
            new Template("demolition", "拆墙", 1, 3),
            new Template("slotting", "开槽", 1, 2),
            new Template("plumbing", "水电改造", 1, 5),
            new Template("tiling", "贴瓷砖", 1, 7),
            new Template("ceiling", "做吊顶", 1, 4),
            new Template("putty", "刮腻子", 1, 5),
            new Template("custom", "全屋定制", 1, 15),
            new Template("cleaning", "保洁", 1, 2)
    );

    private static final List<Template> SOFT_STAGES = List.of(
            new Template("furniture", "家具进场", 2, 3),
            new Template("curtain", "窗帘布艺", 2, 2),
            new Template("lighting", "灯具安装", 2, 2),
            new Template("decor", "软装饰品", 2, 2)
    );

    /**
     * 按装修方式生成阶段计划（幂等：已生成过则直接返回已有阶段）
     *
     * @param mode     1 硬装、2 软装、3 硬装+软装整装
     * @param startDate 计划开工日期，为空取今天
     */
    public List<ConstructionStage> initStages(int serviceId, int mode, int managerId, LocalDate startDate) {
        List<ConstructionStage> exist = constructionStageMapper.findByService(serviceId);
        if (exist != null && !exist.isEmpty()) {
            return exist;
        }
        List<Template> templates = resolveTemplates(mode);
        LocalDate cursor = startDate == null ? LocalDate.now() : startDate;
        int seq = 1;
        for (Template t : templates) {
            ConstructionStage stage = new ConstructionStage();
            stage.setService_id(serviceId);
            stage.setStage_code(t.code());
            stage.setStage_name(t.name());
            stage.setStage_type(t.type());
            stage.setSeq(seq++);
            stage.setPlan_start_date(cursor);
            stage.setPlan_end_date(cursor.plusDays(t.days() - 1L));
            stage.setPlan_days(t.days());
            stage.setStatus(STAGE_NOT_STARTED);
            stage.setAccept_status(ACCEPT_WAIT);
            stage.setRework_status(0);
            stage.setManager_id(managerId);
            stage.setIs_deleted(0);
            constructionStageMapper.insert(stage);
            cursor = cursor.plusDays(t.days());
        }
        return constructionStageMapper.findByService(serviceId);
    }

    /**
     * 硬装 / 软装 / 整装 三种组合，保洁始终排在软装之前
     */
    private List<Template> resolveTemplates(int mode) {
        List<Template> result = new ArrayList<>();
        if (mode == 2) {
            result.addAll(SOFT_STAGES);
            result.add(new Template("cleaning", "保洁", 1, 2));
            return result;
        }
        for (Template t : HARD_STAGES) {
            if (!"cleaning".equals(t.code())) {
                result.add(t);
            }
        }
        result.add(new Template("cleaning", "保洁", 1, 2));
        if (mode == 3) {
            result.addAll(SOFT_STAGES);
        }
        return result;
    }

    public List<ConstructionStage> findByService(int serviceId) {
        return constructionStageMapper.findByService(serviceId);
    }

    public ConstructionStage findById(int id) {
        return constructionStageMapper.findById(id);
    }

    /**
     * 开工：前序阶段必须全部通过业主验收
     */
    public ConstructionStage start(int stageId, int managerId) {
        ConstructionStage stage = requireStage(stageId);
        List<ConstructionStage> all = constructionStageMapper.findByService(stage.getService_id());
        for (ConstructionStage s : all) {
            if (s.getSeq() < stage.getSeq() && s.getStatus() != STAGE_ACCEPTED) {
                throw new IllegalArgumentException("前置阶段「" + s.getStage_name() + "」尚未通过业主验收，不能开工");
            }
        }
        int rows = constructionStageMapper.start(stageId, LocalDate.now());
        if (rows == 0) {
            throw new IllegalArgumentException("该阶段已开工或已完工，请刷新后重试");
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(stage.getService_id());
        if (order.getStatus() == DesignServiceOrderService.STATUS_CONTRACTED) {
            designServiceOrderService.markBuilding(order.getId());
        }
        return constructionStageMapper.findById(stageId);
    }

    /**
     * 完工：回填实际工期、超期天数与是否按时，之后等待业主验收
     */
    public ConstructionStage finish(int stageId, String images, String remark) {
        ConstructionStage stage = requireStage(stageId);
        if (stage.getStatus() != STAGE_DOING && stage.getStatus() != STAGE_REWORK) {
            throw new IllegalArgumentException("只有施工中或返工中的阶段可以报完工");
        }
        LocalDate end = LocalDate.now();
        int actualDays = 0;
        LocalDate actualStart = stage.getActual_start_date() == null
                ? stage.getRework_start_date() : stage.getActual_start_date();
        if (actualStart != null) {
            actualDays = (int) ChronoUnit.DAYS.between(actualStart, end) + 1;
        }
        int delayDays = 0;
        int onTime = ON_TIME_UNKNOWN;
        if (stage.getPlan_end_date() != null) {
            delayDays = (int) Math.max(0, ChronoUnit.DAYS.between(stage.getPlan_end_date(), end));
            onTime = delayDays > 0 ? ON_TIME_DELAY : ON_TIME_YES;
        }
        int rows = constructionStageMapper.finish(stageId, end, actualDays, delayDays, onTime, images, remark);
        if (rows == 0) {
            throw new IllegalArgumentException("该阶段状态已变化，请刷新后重试");
        }
        return constructionStageMapper.findById(stageId);
    }

    /**
     * 业主验收
     *
     * @param pass       true 通过，false 不通过并进入返工
     * @param reworkDays 不通过时填预计返工天数
     */
    public ConstructionStage accept(int stageId, int userId, boolean pass, String remark, Integer reworkDays) {
        ConstructionStage stage = requireStage(stageId);
        DesignServiceOrder order = designServiceOrderService.requireOrder(stage.getService_id());
        if (order.getUser_id() != userId) {
            throw new IllegalArgumentException("只有业主可以验收施工阶段");
        }
        if (stage.getStatus() != STAGE_DONE) {
            throw new IllegalArgumentException("该阶段尚未报完工，暂不能验收");
        }
        LocalDateTime now = LocalDateTime.now();
        if (pass) {
            int rows = constructionStageMapper.acceptPass(stageId, userId, now, remark);
            if (rows == 0) {
                throw new IllegalArgumentException("该阶段状态已变化，请刷新后重试");
            }
            checkReadyPayFinal(stage.getService_id());
        } else {
            if (remark == null || remark.isBlank()) {
                throw new IllegalArgumentException("验收不通过时请填写原因");
            }
            int days = reworkDays == null || reworkDays < 1 ? 1 : reworkDays;
            int rows = constructionStageMapper.acceptReject(stageId, userId, now, remark, days, LocalDate.now());
            if (rows == 0) {
                throw new IllegalArgumentException("该阶段状态已变化，请刷新后重试");
            }
        }
        return constructionStageMapper.findById(stageId);
    }

    /**
     * 返工完成，重新提交验收
     */
    public ConstructionStage reworkDone(int stageId, LocalDate endDate) {
        ConstructionStage stage = requireStage(stageId);
        if (stage.getStatus() != STAGE_REWORK) {
            throw new IllegalArgumentException("该阶段不在返工中");
        }
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        int rows = constructionStageMapper.reworkDone(stageId, end);
        if (rows == 0) {
            throw new IllegalArgumentException("该阶段状态已变化，请刷新后重试");
        }
        return constructionStageMapper.findById(stageId);
    }

    /**
     * 调整计划起止日期（未开工 / 施工中可调）
     */
    public ConstructionStage updatePlan(int stageId, LocalDate start, LocalDate end) {
        ConstructionStage stage = requireStage(stageId);
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("计划结束日期不能早于开始日期");
        }
        int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
        int rows = constructionStageMapper.updatePlan(stageId, start, end, days);
        if (rows == 0) {
            throw new IllegalArgumentException("该阶段已完工，不能再调整计划");
        }
        return constructionStageMapper.findById(stageId);
    }

    /**
     * 进入「待付尾款」的判定
     * <p>
     * 业务口径是「保洁完成付尾款」：硬装 / 整装一旦保洁阶段通过业主验收，
     * 即便软装（家具/窗帘/灯具/饰品）还在进行，也允许业主支付尾款；
     * 纯软装没有保洁阶段，则等全部阶段验收通过后再进入待付尾款。
     */
    private void checkReadyPayFinal(int serviceId) {
        List<ConstructionStage> all = constructionStageMapper.findByService(serviceId);
        if (all == null || all.isEmpty()) {
            return;
        }
        boolean hasCleaning = false;
        boolean cleaningAccepted = false;
        boolean allAccepted = true;
        for (ConstructionStage s : all) {
            if ("cleaning".equals(s.getStage_code())) {
                hasCleaning = true;
                cleaningAccepted = s.getStatus() == STAGE_ACCEPTED;
            }
            if (s.getStatus() != STAGE_ACCEPTED) {
                allAccepted = false;
            }
        }
        if ((hasCleaning && cleaningAccepted) || allAccepted) {
            designServiceOrderService.markWaitFinal(serviceId);
        }
    }

    private ConstructionStage requireStage(int stageId) {
        ConstructionStage stage = constructionStageMapper.findById(stageId);
        if (stage == null || stage.getIs_deleted() != 0) {
            throw new IllegalArgumentException("施工阶段不存在");
        }
        return stage;
    }
}
