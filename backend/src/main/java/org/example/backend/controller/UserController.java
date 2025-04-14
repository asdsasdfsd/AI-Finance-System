// src/main/java/org/example/backend/controller/UserController.java
package org.example.backend.controller;

import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Integer id) {
        User user = userService.findById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/department/{departmentId}")
    public List<User> getByDepartment(@PathVariable Integer departmentId) {
        Department department = departmentService.findById(departmentId);
        return userService.findByDepartment(department);
    }
    
    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userService.getUserRoles(user));
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        if (userService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        user.setUserId(id);
        return ResponseEntity.ok(userService.updateUser(user));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        if (userService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/roles/{roleName}")
    public ResponseEntity<Void> assignRole(@PathVariable Integer id, @PathVariable String roleName) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.assignRole(user, roleName);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}/roles/{roleName}")
    public ResponseEntity<Void> removeRole(@PathVariable Integer id, @PathVariable String roleName) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.removeRole(user, roleName);
        return ResponseEntity.ok().build();
    }
}