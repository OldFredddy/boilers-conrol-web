package com.chsbk.boilers_web.repository;

import com.chsbk.boilers_web.entities.Boiler;
import com.chsbk.boilers_web.entities.BoilerLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BoilerLogRepository extends MongoRepository<BoilerLog, String> {
    BoilerLog findByBoiler(Boiler boiler);
}
