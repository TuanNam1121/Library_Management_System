package com.library.management.repositories;

import com.library.management.dto.LoginRequestDTO;
import com.library.management.entities.Author;
import com.library.management.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("FROM Author a WHERE a.name LIKE CONCAT('%', :keyword, '%')")
    List<Author> searchByName(@Param("keyword")String keyword);

}
