package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationCategory;
import com.example.decoratebackservice.entity.DecorationMaterial;
import com.example.decoratebackservice.entity.Supplier;
import com.example.decoratebackservice.mapper.DecorationCategoryMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DecorationMaterialService;
import com.example.decoratebackservice.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 库管人员的材料管理接口。
 * <p>
 * 与公开的 /material/** 隔离：
 * <ul>
 *     <li>公开接口供商城浏览，只关心在架材料</li>
 *     <li>本接口供库管维护库存，需要看到下架、待审核的材料，且必须校验库管身份</li>
 * </ul>
 * 每个方法独立校验 ROLE_STOCK，不依赖前端隐藏按钮——前端校验只是体验，
 * 真正的边界在后端。
 */
@Tag(name = "库管-材料管理接口")
@RestController
@RequestMapping("/stock")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StockController {

    /**
     * 分类类型：2 表示装修材料分类（1 为装修方案分类）
     */
    private static final int CATEGORY_TYPE_MATERIAL = 2;

    private final DecorationMaterialService materialService;
    private final SupplierService supplierService;
    private final DecorationCategoryMapper categoryMapper;

    public StockController(DecorationMaterialService materialService,
                           SupplierService supplierService,
                           DecorationCategoryMapper categoryMapper) {
        this.materialService = materialService;
        this.supplierService = supplierService;
        this.categoryMapper = categoryMapper;
    }

    /**
     * 库管上传材料时需要的下拉数据。
     * <p>
     * 项目原本没有任何分类查询接口（decoration_category 没有对应的 Controller），
     * 而 decoration_material.category_id 是 NOT NULL 且带外键，必须真实存在，
     * 因此这里一次性把分类与供应商都返回，前端无需再发两次请求。
     */
    @Operation(summary = "库管下拉选项：材料分类 + 供应商")
    @GetMapping("/options")
    public Result<Map<String, Object>> options(Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可访问材料管理");
        }
        List<DecorationCategory> categories = categoryMapper.findByType(CATEGORY_TYPE_MATERIAL);
        List<Supplier> suppliers = supplierService.findAll();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categories", categories);
        data.put("suppliers", suppliers);
        return Result.success(data);
    }

    @Operation(summary = "库管查询全部材料（含下架、待审核）")
    @GetMapping("/materials")
    public Result<List<DecorationMaterial>> materials(Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可访问材料管理");
        }
        // 逻辑删除的材料仍在表里，此处过滤掉，避免删除后刷新又重新出现
        List<DecorationMaterial> materials = materialService.findAll().stream()
                .filter(item -> item.getIs_deleted() == 0)
                .toList();
        return Result.success(materials);
    }

    @Operation(summary = "库管上传新材料")
    @PostMapping("/materials")
    public Result<Integer> insert(@RequestBody DecorationMaterial material, Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可上传材料");
        }
        String error = validate(material);
        if (error != null) {
            return Result.error(400, error);
        }
        // 库管是内部人员，上传即视为已通过审核；上架状态默认上架，可由前端覆盖
        material.setAudit_status(1);
        material.setAudit_remark(null);
        if (material.getStatus() != 0) {
            material.setStatus(1);
        }
        material.setSales_count(0);
        material.setIs_deleted(0);
        return Result.success("材料上传成功", materialService.insert(material));
    }

    @Operation(summary = "库管修改材料基础信息（名称/价格/描述/单位/分类/供应商）")
    @PutMapping("/materials")
    public Result<Integer> update(@RequestBody DecorationMaterial material, Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可修改材料");
        }
        if (material.getId() <= 0) {
            return Result.error(400, "缺少材料ID");
        }
        if (materialService.findById(material.getId()) == null) {
            return Result.error(404, "材料不存在");
        }
        return Result.success("修改成功", materialService.update(material));
    }

    /**
     * 单独调整库存。
     * <p>
     * 既有的 update 语句用 {@code <if test='stock != 0'>} 判断，无法把库存改为 0，
     * 因此库存走这条专用接口，参数为原始 int，0 也能生效。
     */
    @Operation(summary = "库管调整库存数量（支持调整为0）")
    @PutMapping("/materials/{id}/stock")
    public Result<Integer> updateStock(@PathVariable int id,
                                       @RequestParam("stock") int stock,
                                       Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可调整库存");
        }
        if (stock < 0) {
            return Result.error(400, "库存数量不能为负数");
        }
        if (materialService.findById(id) == null) {
            return Result.error(404, "材料不存在");
        }
        return Result.success("库存已更新", materialService.updateStock(id, stock));
    }

    /**
     * 上下架。同样因为既有 update 无法写入 status = 0，单独提供该接口
     *
     * @param status 0下架 1上架
     */
    @Operation(summary = "库管上架/下架材料")
    @PutMapping("/materials/{id}/status")
    public Result<Integer> updateStatus(@PathVariable int id,
                                        @RequestParam("status") int status,
                                        Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可上下架材料");
        }
        if (status != 0 && status != 1) {
            return Result.error(400, "上架状态只能是 0 或 1");
        }
        if (materialService.findById(id) == null) {
            return Result.error(404, "材料不存在");
        }
        return Result.success(status == 1 ? "已上架" : "已下架", materialService.updateStatus(id, status));
    }

    @Operation(summary = "库管删除材料（逻辑删除）")
    @DeleteMapping("/materials/{id}")
    public Result<Integer> delete(@PathVariable int id, Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可删除材料");
        }
        if (materialService.findById(id) == null) {
            return Result.error(404, "材料不存在");
        }
        // 用逻辑删除而不是物理删除：material_stock_record 的外键是 ON DELETE RESTRICT，
        // 有流水记录的材料一旦物理删除就会直接抛数据库异常
        return Result.success("删除成功", materialService.softDelete(id));
    }

    /**
     * 上传前的必填校验。
     * <p>
     * decoration_material 有多列 NOT NULL 且带外键，缺任何一项都会在数据库层报错，
     * 提前拦下可以返回可读的提示，而不是一串 SQL 异常。
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
        if (material.getSupplier_id() <= 0) {
            return "请选择供应商";
        }
        if (material.getStock() < 0) {
            return "库存数量不能为负数";
        }
        return null;
    }
}
