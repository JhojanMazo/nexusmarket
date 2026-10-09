package com.nexusmarket.domain.repository;

import com.nexusmarket.domain.model.User;
public interface UserRepository {
    User save(User user);
    boolean existsByEmail(String email);
    boolean existsByIdentityDocument(String doc);
    User findById(String id);


    java.util.Optional<com.nexusmarket.domain.model.User> findByEmail(String email);

}
