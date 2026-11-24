package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeRepository extends JpaRepository<Cafe, Integer> {

    List<Cafe> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name, String address);
}
