// backend/src/main/java/org/example/backend/service/RoleService.java
package org.example.backend.service;

import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Role Service - 最终修复版本
 * 
 * 解决了RoleRepository.findByName()返回Optional<Role>的类型兼容性问题
 */
@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }
    
    /**
     * 根据名称查找角色 - 修复返回类型兼容性
     * 
     * @param name 角色名称
     * @return 角色对象，如果未找到返回null（保持向后兼容）
     */
    public Role findByName(String name) {
        Optional<Role> roleOptional = roleRepository.findByName(name);
        return roleOptional.orElse(null);
    }
    
    /**
     * 根据名称查找角色 - 返回Optional版本
     * 
     * @param name 角色名称
     * @return Optional包装的角色对象
     */
    public Optional<Role> findOptionalByName(String name) {
        return roleRepository.findByName(name);
    }
    
    /**
     * 检查角色名是否存在
     * 
     * @param name 角色名称
     * @return 如果存在返回true
     */
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public void deleteById(Integer id) {
        roleRepository.deleteById(id);
    }
}