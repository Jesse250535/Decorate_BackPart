package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.Supplier;
import com.example.decoratebackservice.mapper.SupplierMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 材料供应商业务层
 */
@Service
public class SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;

    public int insert(Supplier supplier) {
        return supplierMapper.insert(supplier);
    }

    public Supplier findById(int id) {
        return supplierMapper.findById(id);
    }

    public List<Supplier> findAll() {
        return supplierMapper.findAll();
    }

    public int update(Supplier supplier) {
        return supplierMapper.update(supplier);
    }

    public int delete(int id) {
        return supplierMapper.delete(id);
    }
}
