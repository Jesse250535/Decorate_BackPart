package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationCategory;
import com.example.decoratebackservice.entity.DecorationMaterial;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.DecorationCategoryMapper;
import com.example.decoratebackservice.mapper.DecorationMaterialMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DecorationMaterialService;
import com.example.decoratebackservice.service.SupplierService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 材料供应商工作台。
 * <p>
 * 与库管（/stock）明确分工：
 * <ul>
 *     <li>材料供应商：上传/修改自己供应的材料，上传后为「待审核」，不能自己上架</li>
 *     <li>库管人员：审核材料供应商上传的材料（见 {@link MaterialAuditController}）</li>
 * </ul>
 * 原来「上传材料」放在库管页面，实际上传动作应由供应商完成，库管只负责把关。
 */
@Tag(name = "材料供应商-材料上传接口")
@RestController
@RequestMapping("/supplier-portal")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupplierPortalController {

    /**
     * 分类类型：2 装修材料
     */
    private static final int CATEGORY_TYPE_MATERIAL = 2;

    /**
     * 审核状态：待审核
     */
    private static final int AUDIT_PENDING = 0;

    private final DecorationMaterialService materialService;
    private final DecorationMaterialMapper materialMapper;
    private final DecorationCategoryMapper categoryMapper;
    private final SupplierService supplierService;
    private final UserService userService;

    public SupplierPortalController(DecorationMaterialService materialService,
                                    DecorationMaterialMapper materialMapper,
                                    DecorationCategoryMapper categoryMapper,
                                    SupplierService supplierService,
                                    UserService userService) {
        this.materialService = materialService;
        this.materialMapper = materialMapper;
        this.categoryMapper = categoryMapper;
        this.supplierService = supplierService;
        this.userService = userService;
    }

    @Operation(summary = "材料供应商上传材料所需下拉数据：材料分类")
    @GetMapping("/options")
    public Result<Map<String, Object>> options(Authentication authentication) {
        if (!StaffRole.isSupplier(authentication)) {
            return Result.forbidden("仅材料供应商可上传材料");
        }
        List<DecorationCategory> categories = categoryMapper.findByType(CATEGORY_TYPE_MATERIAL);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categories", categories);
        return Result.success(data);
    }

    @Operation(summary = "材料供应商查询自己上传的材料（含待审核/已驳回/已通过）")
    @GetMapping("/materials")
    public Result<List<DecorationMaterial>> materials(Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            return Result.forbidden("仅材料供应商可上传材料");
        }
        return Result.success(materialMapper.findByCreateUserId(user.getId()));
    }

    @Operation(summary = "材料供应商上传材料（落库为待审核，由库管审核）")
    @PostMapping("/materials")
    public Result<Integer> insert(@RequestBody DecorationMaterial material, Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            return Result.forbidden("仅材料供应商可上传材料");
        }
        String error = validate(material);
        if (error != null) {
            return Result.error(400, error);
        }
        // 上传人 = 当前登录供应商账号；供应商档案按账号自动挂接
        int supplierId = supplierService.findOrCreateByDisplayName(
                user.getNickname() == null || user.getNickname().isBlank()
                        ? user.getUsername() : user.getNickname(),
                user.getPhone());
        material.setSupplier_id(supplierId);
        material.setCreate_user_id(user.getId());
        // 供应商没有上架权限：审核通过前状态一律为下架，避免未审核材料直接出现在商城
        material.setAudit_status(AUDIT_PENDING);
        material.setAudit_remark(null);
        material.setStatus(0);
        material.setSales_count(0);
        material.setIs_deleted(0);
        return Result.success("材料已提交，等待库管审核", materialService.insert(material));
    }

    @Operation(summary = "材料供应商修改自己上传的材料（修改后重新回到待审核）")
    @PutMapping("/materials")
    public Result<Integer> update(@RequestBody DecorationMaterial material, Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            return Result.forbidden("仅材料供应商可上传材料");
        }
        if (material.getId() <= 0) {
            return Result.error(400, "缺少材料ID");
        }
        DecorationMaterial exists = materialService.findById(material.getId());
        if (exists == null || exists.getIs_deleted() != 0) {
            return Result.error(404, "材料不存在");
        }
        // 只能改自己的材料，防止用别人的ID覆盖他人数据
        if (exists.getCreate_user_id() != user.getId()) {
            return Result.forbidden("只能修改自己上传的材料");
        }
        if (exists.getAudit_status() == 1) {
            return Result.error(400, "该材料已审核通过，如需修改请联系库管下架后再改");
        }
        String error = validate(material);
        if (error != null) {
            return Result.error(400, error);
        }
        // 供应商资料不允许被覆盖：供应商、上传人、上架状态由系统控制
        material.setSupplier_id(exists.getSupplier_id());
        material.setCreate_user_id(exists.getCreate_user_id());
        material.setStatus(0);
        material.setIs_deleted(0);
        int rows = materialService.update(material);
        // 改完重新送审：update 的 <if> 无法把 audit_status 写回 0，用专用语句重置
        materialMapper.audit(material.getId(), AUDIT_PENDING, null);
        return Result.success("修改成功，已重新提交审核", rows);
    }

    /**
     * 上传/修改前的必填校验，与库管上传保持同一套规则
     *
     * @return 错误信息；校验通过返回 null
     */
    private String validate(DecorationMaterial material) {
        if (material == null) {
            return "请求体不能为空";
        }
        if (material.getMaterial_name() == null || material.getMaterial_name().isBlank()) {
            return "请填写材料名称";
        }
        if (material.getCover_image() == null || material.getCover_image().isBlank()) {
            return "请上传材料外观图";
        }
        if (material.getPrice() == null) {
            return "请填写材料单价";
        }
        if (material.getDescription() == null || material.getDescription().isBlank()) {
            return "请填写材料参数详情";
        }
        if (material.getCategory_id() <= 0) {
            return "请选择材料分类";
        }
        if (material.getStock() < 0) {
            return "库存数量不能为负数";
        }
        if (material.getUnit() == null || material.getUnit().isBlank()) {
            return "请填写计量单位";
        }
        return null;
    }

    /**
     * 取当前登录账号
     *
     * @return 非材料供应商 / 未登录返回 null
     */
    private User currentUser(Authentication authentication) {
        if (!StaffRole.isSupplier(authentication)) {
            return null;
        }
        return userService.findByUsernameForStaff(StaffRole.username(authentication));
    }
}
