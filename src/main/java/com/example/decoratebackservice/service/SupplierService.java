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

    /**
     * 为「材料供应商」角色账号找到（必要时创建）对应的供应商档案。
     * <p>
     * decoration_material.supplier_id 是 NOT NULL 外键，供应商账号上传材料时
     * 必须落到一条真实的 supplier 记录上。这里以账号昵称/用户名为准做一次
     * find-or-create，供应商无需先去后台维护档案。
     *
     * @param displayName 供应商名称（取账号昵称，缺失时用用户名）
     * @param phone       联系电话，缺失时用占位值（数据库该列 NOT NULL）
     * @return 供应商档案ID
     */
    public int findOrCreateByDisplayName(String displayName, String phone) {
        String name = (displayName == null || displayName.isBlank()) ? "未命名供应商" : displayName.trim();
        for (Supplier supplier : supplierMapper.findAll()) {
            if (supplier.getIs_deleted() == 0 && name.equals(supplier.getSupplier_name())) {
                return supplier.getId();
            }
        }
        Supplier supplier = new Supplier();
        supplier.setSupplier_name(name);
        supplier.setContact_person(name);
        supplier.setContact_phone((phone == null || phone.isBlank()) ? "00000000000" : phone.trim());
        supplier.setAddress(null);
        supplier.setRemark("材料供应商账号上传材料时自动创建");
        supplier.setStatus(1);
        supplier.setIs_deleted(0);
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

}
