package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.VetService;
import java.util.List;

public interface VetServiceRepository {
    List<VetService> findByActiveTrue();
}
