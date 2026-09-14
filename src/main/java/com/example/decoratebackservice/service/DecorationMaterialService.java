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
}
