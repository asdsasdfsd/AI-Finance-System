// backend/src/main/java/org/example/backend/infrastructure/adapter/UserRepositoryDelegate.java
package org.example.backend.infrastructure.adapter;

import org.example.backend.domain.aggregate.user.UserAggregate;
import org.example.backend.domain.aggregate.user.UserAggregateRepository;
import org.example.backend.model.Department;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UserRepository委托 - DDD模式下的简化实现
 * 
 * 只实现必要的方法，其他方法抛出UnsupportedOperationException
 */
@Repository
@Primary
@Profile("ddd")
public class UserRepositoryDelegate implements UserRepository {
    
    private final UserAggregateRepository userAggregateRepository;
    
    public UserRepositoryDelegate(UserAggregateRepository userAggregateRepository) {
        this.userAggregateRepository = userAggregateRepository;
    }
    
    // ========== 核心方法实现 ==========
    
    @Override
    @NonNull
    public Optional<User> findByUsername(String username) {
        return userAggregateRepository.findByUsername(username)
                .map(this::convertToUser);
    }
    
    @Override
    @NonNull
    public Optional<User> findByExternalId(String externalId) {
        return userAggregateRepository.findByExternalId(externalId)
                .map(this::convertToUser);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userAggregateRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userAggregateRepository.existsByEmail(email);
    }
    
    @Override
    @NonNull
    public Optional<User> findById(@NonNull Integer id) {
        return userAggregateRepository.findById(id)
                .map(this::convertToUser);
    }
    
    @Override
    public boolean existsById(@NonNull Integer id) {
        return userAggregateRepository.existsById(id);
    }
    
    @Override
    @NonNull
    public List<User> findAll() {
        return userAggregateRepository.findAll().stream()
                .map(this::convertToUser)
                .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return userAggregateRepository.count();
    }
    
    // ========== 转换方法 ==========
    
    /**
     * 将UserAggregate转换为User实体
     */
    private User convertToUser(UserAggregate userAggregate) {
        User user = new User();
        user.setUserId(userAggregate.getUserId());
        user.setUsername(userAggregate.getUsername());
        user.setEmail(userAggregate.getEmail());
        user.setPassword(userAggregate.getPassword());
        user.setFullName(userAggregate.getFullName());
        user.setEnabled(userAggregate.getEnabled());
        user.setExternalId(userAggregate.getExternalId());
        user.setLastLogin(userAggregate.getLastLogin());
        user.setCreatedAt(userAggregate.getCreatedAt());
        user.setUpdatedAt(userAggregate.getUpdatedAt());
        user.setRoles(userAggregate.getRoles());
        
        return user;
    }
    
    // ========== 不支持的方法 ==========
    
    @Override
    @NonNull
    public List<User> findByDepartment(Department department) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    @NonNull
    public List<User> findAll(@NonNull Sort sort) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    @NonNull
    public Page<User> findAll(@NonNull Pageable pageable) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    @NonNull
    public List<User> findAllById(@NonNull Iterable<Integer> ids) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void deleteById(@NonNull Integer id) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void delete(@NonNull User entity) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void deleteAllById(@NonNull Iterable<? extends Integer> ids) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void deleteAll(@NonNull Iterable<? extends User> entities) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    @NonNull
    public <S extends User> S save(@NonNull S entity) {
        throw new UnsupportedOperationException("Use UserApplicationService.createUser() or updateUser() instead in DDD mode");
    }
    
    @Override
    @NonNull
    public <S extends User> List<S> saveAll(@NonNull Iterable<S> entities) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void flush() {
        userAggregateRepository.flush();
    }
    
    @Override
    @NonNull
    public <S extends User> S saveAndFlush(@NonNull S entity) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    @NonNull
    public <S extends User> List<S> saveAllAndFlush(@NonNull Iterable<S> entities) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void deleteAllInBatch(@NonNull Iterable<User> entities) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void deleteAllByIdInBatch(@NonNull Iterable<Integer> ids) {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("Use UserApplicationService instead in DDD mode");
    }
    
    @Override
    @NonNull
    public User getOne(@NonNull Integer id) {
        return findById(id).orElse(null);
    }
    
    @Override
    @NonNull
    public User getById(@NonNull Integer id) {
        return convertToUser(userAggregateRepository.getReferenceById(id));
    }
    
    @Override
    @NonNull
    public User getReferenceById(@NonNull Integer id) {
        return convertToUser(userAggregateRepository.getReferenceById(id));
    }
    
    @Override
    @NonNull
    public <S extends User> Optional<S> findOne(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in DDD mode");
    }
    
    @Override
    @NonNull
    public <S extends User> List<S> findAll(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in DDD mode");
    }
    
    @Override
    @NonNull
    public <S extends User> List<S> findAll(@NonNull Example<S> example, @NonNull Sort sort) {
        throw new UnsupportedOperationException("Example queries not supported in DDD mode");
    }
    
    @Override
    @NonNull
    public <S extends User> Page<S> findAll(@NonNull Example<S> example, @NonNull Pageable pageable) {
        throw new UnsupportedOperationException("Example queries not supported in DDD mode");
    }
    
    @Override
    public <S extends User> long count(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in DDD mode");
    }
    
    @Override
    public <S extends User> boolean exists(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in DDD mode");
    }
    
    @Override
    @NonNull
    public <S extends User, R> R findBy(@NonNull Example<S> example, @NonNull Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Fluent queries not supported in DDD mode");
    }
}