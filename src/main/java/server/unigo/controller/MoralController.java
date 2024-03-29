package server.unigo.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import server.unigo.dto.MoralsDTO;
import server.unigo.security.CustomUserDetail;
import server.unigo.service.MoralService;

import java.util.List;

@RestController
@Log4j2
public class MoralController {
    private final MoralService moralService;

    @Autowired
    public MoralController(MoralService moralService) {
        this.moralService = moralService;
    }

    //  Get student moral data for client
    @GetMapping("v1/morals/{id}")
    @PreAuthorize("hasAnyAuthority('READ_KQ HTRL')")
    public List<MoralsDTO> getMoral(@PathVariable String id, Authentication authentication) {
        log.info("User with ID="+id+" requested to v1/morals/ to getMoral");
        CustomUserDetail customUserDetail=(CustomUserDetail)authentication.getPrincipal();
        if (customUserDetail.getUsers().getUsername().equals(id))
        return moralService.getMoral(id);
        else
            return null;
    }
}
