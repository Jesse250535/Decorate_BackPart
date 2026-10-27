package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationMaterial;
import com.example.decoratebackservice.mapper.DecorationMaterialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 装修材料业务层
 */
@Service
public class DecorationMaterialService {

    @Autowired
    private DecorationMaterialMapper decorationMaterialMapper;

    public int insert(DecorationMaterial material) {
        return decorationMaterialMapper.insert(material);
    }

    public DecorationMaterial findById(int id) {
        return decorationMaterialMapper.findById(id);
    }

    public List<DecorationMaterial> findAll() {
        return decorationMaterialMapper.findAll();
    }

    public int update(DecorationMaterial material) {
        return decorationMaterialMapper.update(material);
    }

    public int delete(int id) {
        return decorationMaterialMapper.delete(id);
    }

    /**
     * 修改上架状态（0下架 1上架）
     * <p>
     * 单独暴露是因为通用 update 会把 status=0 当作「未提供」而跳过
     */
    public int updateStatus(int id, int status) {
        return decorationMaterialMapper.updateStatus(id, status);
    }

    /**
     * 修改库存数量（支持 0）
     * <p>
     * 理由同上：通用 update 无法把库存改为 0
     */
    public int updateStock(int id, int stock) {
        return decorationMaterialMapper.updateStock(id, stock);
    }

    /**
     * 逻辑删除材料（保留库存流水）
     * <p>
     * 与物理 delete 并存：物理删除仍给既有调用方使用，库管页面走逻辑删除，
     * 避免 material_stock_record 的外键约束导致删除失败
     */
    public int softDelete(int id) {
        return decorationMaterialMapper.softDelete(id);
    }
}
