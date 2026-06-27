package com.library.management.repositories;

import com.library.management.dto.LoginRequestDTO;
import com.library.management.entities.Author;
import com.library.management.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

}
