package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.DessertPlate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DessertPlateRepository extends JpaRepository<DessertPlate, Integer> {

    Optional<DessertPlate> findFirstByUserIdOrderByPlateOrderAsc(Integer userId);
}
