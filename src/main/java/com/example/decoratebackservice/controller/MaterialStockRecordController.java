package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.MaterialStockRecord;
import com.example.decoratebackservice.service.MaterialStockRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 材料出入库记录管理接口
 */
@Tag(name = "材料出入库记录管理接口")
@RestController
@RequestMapping("/stockRecord")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MaterialStockRecordController {

    @Autowired
    private MaterialStockRecordService materialStockRecordService;

    @Operation(summary = "添加出入库记录")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody MaterialStockRecord record) {
        return Result.success(materialStockRecordService.insert(record));
    }

    @Operation(summary = "根据ID查询出入库记录")
    @GetMapping("/findById/{id}")
    public Result<MaterialStockRecord> findById(@PathVariable int id) {
        return Result.success(materialStockRecordService.findById(id));
    }

    @Operation(summary = "更新出入库记录")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody MaterialStockRecord record) {
        return Result.success(materialStockRecordService.update(record));
    }

    @Operation(summary = "根据ID删除出入库记录")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(materialStockRecordService.delete(id));
    }

    @Operation(summary = "查询所有出入库记录")
    @GetMapping("/findAll")
    public Result<List<MaterialStockRecord>> findAll() {
        return Result.success(materialStockRecordService.findAll());
    }
}
