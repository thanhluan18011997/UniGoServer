package server.unigo.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import server.unigo.dto.PersonalInformationsDTO;
import server.unigo.security.CustomUserDetail;
import server.unigo.service.PersonalInformationService;

@Log4j2
@RestController
public class PersonalInformationController {
    private final PersonalInformationService personalInformationService;

    @Autowired
    public PersonalInformationController(PersonalInformationService personalInformationService) {
        this.personalInformationService = personalInformationService;
    }


    //  Get PersonalInformation data for client
    @GetMapping("/v1/personal_informations/{id}")
    @PreAuthorize("hasAnyAuthority('READ_PersonalInformation')")
    public PersonalInformationsDTO getPersonalInformation(@PathVariable String id, Authentication authentication) {
        log.info("User with ID=" + id + " requested to v1/personal_informations/ to getPersonalInformation");
        CustomUserDetail customUserDetail=(CustomUserDetail)authentication.getPrincipal();
        if (customUserDetail.getUsers().getUsername().equals(id))
            return personalInformationService.getPersonalInformations(id);
        else
            return null;
    }
}
