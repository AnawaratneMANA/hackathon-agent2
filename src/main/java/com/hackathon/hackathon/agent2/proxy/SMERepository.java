package com.hackathon.hackathon.agent2.proxy;

import com.hackathon.hackathon.agent2.domain.SME;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SMERepository extends JpaRepository<SME, Long> {
    Optional<SME> findBySmeCode(String smeCode);
}