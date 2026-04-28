package com.lil.safetagv2reviewservice.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "rpps-service", url = "${services.rppsService}")
public interface RppsClient {

    // On utilise exactement la route de ton PractitionerController
    @GetMapping("/{rppsId}")
    Object getPractitionerByRpps(@PathVariable("rppsId") String rppsId);

}
