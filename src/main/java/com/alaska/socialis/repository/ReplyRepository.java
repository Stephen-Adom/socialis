package com.alaska.socialis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alaska.socialis.model.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

}
