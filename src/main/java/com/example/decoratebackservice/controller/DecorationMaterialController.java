package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationMaterial;
import com.example.decoratebackservice.service.DecorationMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 装修材料管理接口
 */
@Tag(name = "装修材料管理接口")
@RestController
@RequestMapping("/material")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DecorationMaterialController {

    @Autowired
    private DecorationMaterialService decorationMaterialService;

    @Operation(summary = "添加装修材料")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody DecorationMaterial material) {
        return Result.success(decorationMaterialService.insert(material));
    }

    @Operation(summary = "根据ID查询装修材料")
    @GetMapping("/findById/{id}")
    public Result<DecorationMaterial> findById(@PathVariable int id) {
        return Result.success(decorationMaterialService.findById(id));
    }

    @Operation(summary = "更新装修材料")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody DecorationMaterial material) {
        return Result.success(decorationMaterialService.update(material));
    }

    @Operation(summary = "根据ID删除装修材料")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(decorationMaterialService.delete(id));
    }

    @Operation(summary = "查询所有装修材料")
    @GetMapping("/findAll")
    public Result<List<DecorationMaterial>> findAll() {
        return Result.success(decorationMaterialService.findAll());
    }


}
