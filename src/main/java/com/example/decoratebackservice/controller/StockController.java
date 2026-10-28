package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationCategory;
import com.example.decoratebackservice.entity.DecorationMaterial;
import com.example.decoratebackservice.entity.DecorationPlan;
import com.example.decoratebackservice.entity.Supplier;
import com.example.decoratebackservice.mapper.DecorationCategoryMapper;
import com.example.decoratebackservice.mapper.DecorationPlanMapper;
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
    private final DecorationPlanMapper planMapper;

    public StockController(DecorationMaterialService materialService,
                           SupplierService supplierService,
                           DecorationCategoryMapper categoryMapper,
                           DecorationPlanMapper planMapper) {
        this.materialService = materialService;
        this.supplierService = supplierService;
        this.categoryMapper = categoryMapper;
        this.planMapper = planMapper;
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

    // ===== 分类管理（decoration_category）=====

    /**
     * 分类列表。
     *
     * @param categoryType 1装修方案分类 2装修材料分类；省略或 0 表示两种类型都返回
     */
    @Operation(summary = "库管查询分类列表（默认返回全部类型）")
    @GetMapping("/categories")
    public Result<List<DecorationCategory>> categories(
            @RequestParam(value = "category_type", required = false) Integer categoryType,
            Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可管理分类");
        }
        int type = categoryType == null ? 0 : categoryType;
        if (type != 0 && type != 1 && type != 2) {
            return Result.error(400, "分类类型只能是 1（装修方案）或 2（装修材料）");
        }
        List<DecorationCategory> list = categoryMapper.findAll().stream()
                .filter(item -> item.getIs_deleted() == 0)
                .filter(item -> type == 0 || item.getCategory_type() == type)
                .toList();
        return Result.success(list);
    }

    @Operation(summary = "库管新增分类")
    @PostMapping("/categories")
    public Result<Integer> insertCategory(@RequestBody DecorationCategory category,
                                          Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可管理分类");
        }
        String error = validateCategory(category, false);
        if (error != null) {
            return Result.error(400, error);
        }
        category.setCategory_name(category.getCategory_name().trim());
        if (nameExists(category.getCategory_name(), category.getCategory_type(), 0)) {
            return Result.error(400, "同类型下已存在同名分类");
        }
        return Result.success("分类已新增", categoryMapper.insert(category));
    }

    @Operation(summary = "库管修改分类")
    @PutMapping("/categories")
    public Result<Integer> updateCategory(@RequestBody DecorationCategory category,
                                          Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可管理分类");
        }
        String error = validateCategory(category, true);
        if (error != null) {
            return Result.error(400, error);
        }
        DecorationCategory exists = categoryMapper.findById(category.getId());
        if (exists == null || exists.getIs_deleted() == 1) {
            return Result.error(404, "分类不存在或已删除");
        }
        // 自己不能挂到自己下面，避免形成环
        if (category.getParent_id() == category.getId()) {
            return Result.error(400, "父分类不能是自己");
        }
        category.setCategory_name(category.getCategory_name().trim());
        if (nameExists(category.getCategory_name(), category.getCategory_type(), category.getId())) {
            return Result.error(400, "同类型下已存在同名分类");
        }
        return Result.success("分类已更新", categoryMapper.update(category));
    }

    /**
     * 逻辑删除分类。
     * <p>
     * decoration_material / decoration_plan 的 category_id 都是 ON DELETE RESTRICT 外键，
     * 因此这里只允许删除没有被引用的分类，避免出现「材料指向一个不存在的分类」。
     */
    @Operation(summary = "库管删除分类（逻辑删除）")
    @DeleteMapping("/categories/{id}")
    public Result<Integer> deleteCategory(@PathVariable int id, Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可管理分类");
        }
        DecorationCategory exists = categoryMapper.findById(id);
        if (exists == null || exists.getIs_deleted() == 1) {
            return Result.error(404, "分类不存在或已删除");
        }
        long materialUsed = materialService.findAll().stream()
                .filter(item -> item.getIs_deleted() == 0 && item.getCategory_id() == id)
                .count();
        if (materialUsed > 0) {
            return Result.error(400, "该分类下还有 " + materialUsed + " 件材料，无法删除");
        }
        long planUsed = planMapper.findAll().stream()
                .filter(item -> item.getIs_deleted() == 0 && item.getCategory_id() == id)
                .count();
        if (planUsed > 0) {
            return Result.error(400, "该分类下还有 " + planUsed + " 个装修方案，无法删除");
        }
        long children = categoryMapper.findAll().stream()
                .filter(item -> item.getIs_deleted() == 0 && item.getParent_id() == id)
                .count();
        if (children > 0) {
            return Result.error(400, "该分类下还有 " + children + " 个子分类，请先删除子分类");
        }
        return Result.success("分类已删除", categoryMapper.softDelete(id));
    }

    /**
     * 同类型下是否已存在同名（且未删除）分类
     *
     * @param excludeId 修改时排除自身，新增传 0
     */
    private boolean nameExists(String name, int categoryType, int excludeId) {
        return categoryMapper.findAll().stream()
                .anyMatch(item -> item.getIs_deleted() == 0
                        && item.getCategory_type() == categoryType
                        && item.getId() != excludeId
                        && name.equals(item.getCategory_name()));
    }

    /**
     * 分类字段校验
     *
     * @param requireId 修改时为 true，新增时为 false
     * @return 错误信息；校验通过返回 null
     */
    private String validateCategory(DecorationCategory category, boolean requireId) {
        if (category == null) {
            return "请求体不能为空";
        }
        if (requireId && category.getId() <= 0) {
            return "缺少分类ID";
        }
        if (category.getCategory_name() == null || category.getCategory_name().isBlank()) {
            return "请填写分类名称";
        }
        if (category.getCategory_name().trim().length() > 50) {
            return "分类名称不能超过 50 个字符";
        }
        if (category.getCategory_type() != 1 && category.getCategory_type() != 2) {
            return "分类类型只能是 1（装修方案）或 2（装修材料）";
        }
        if (category.getParent_id() < 0) {
            return "父分类不存在";
        }
        if (category.getSort() < 0) {
            return "排序权重不能为负数";
        }
        if (category.getIs_hot() != 0 && category.getIs_hot() != 1) {
            return "热门标记只能是 0 或 1";
        }
        return null;
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
