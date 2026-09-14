package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.MaterialStockRecord;
import com.example.decoratebackservice.mapper.MaterialStockRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 材料出入库记录业务层
 */
@Service
public class MaterialStockRecordService {

    @Autowired
    private MaterialStockRecordMapper materialStockRecordMapper;

    public int insert(MaterialStockRecord record) {
        return materialStockRecordMapper.insert(record);
    }

    public MaterialStockRecord findById(int id) {
        return materialStockRecordMapper.findById(id);
    }

    public List<MaterialStockRecord> findAll() {
        return materialStockRecordMapper.findAll();
    }

    public int update(MaterialStockRecord record) {
        return materialStockRecordMapper.update(record);
    }

    public int delete(int id) {
        return materialStockRecordMapper.delete(id);
    }
}
