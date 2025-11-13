package com.hackathon.hackathon.agent2.proxy;

import com.hackathon.hackathon.agent2.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
}
