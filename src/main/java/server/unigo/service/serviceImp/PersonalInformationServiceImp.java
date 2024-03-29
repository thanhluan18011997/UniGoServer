package server.unigo.service.serviceImp;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import server.unigo.dto.PersonalInformationsDTO;
import server.unigo.map.PersonalInformationMapper;
import server.unigo.model.PersonalInformations;
import server.unigo.model.Users;
import server.unigo.repository.PersonalInformationRepository;
import server.unigo.repository.UserRepository;
import server.unigo.service.PersonalInformationService;

import java.util.Arrays;
import java.util.Optional;

@Service
public class PersonalInformationServiceImp implements PersonalInformationService {
    private final RestTemplate restTemplate;
    private final PersonalInformationRepository personalInformationRepository;
    private final UserRepository userRepository;

    @Autowired
    public PersonalInformationServiceImp(RestTemplate restTemplate, PersonalInformationRepository personalInformationRepository, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.personalInformationRepository = personalInformationRepository;
        this.userRepository = userRepository;
    }

    //when using generic, appear cast error :" return hash map instead of json "
    @Override
    public void savePersonalInformation(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<PersonalInformationsDTO> entity = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://dnunigo.herokuapp.com/dut/")
                .queryParam("command", "get_personal_information")
                .queryParam("session_id", id)
                .queryParam("semester_id", "2010");
        ResponseEntity<PersonalInformationsDTO> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, PersonalInformationsDTO.class);
        PersonalInformationsDTO personalInformationsDTO = responseEntity.getBody();
        PersonalInformationMapper personalInformationMapper = Mappers.getMapper(PersonalInformationMapper.class);
        PersonalInformations personalInformations = personalInformationMapper.mapDTOtoEntity(personalInformationsDTO);
        Users user = userRepository.findByUsername(id).get();
        personalInformations.setUser(user);
        personalInformationRepository.save(personalInformations);

    }

    @Override
    public PersonalInformationsDTO getPersonalInformations(String id) {
        PersonalInformationMapper personalInformationMapper = Mappers.getMapper(PersonalInformationMapper.class);
        Optional<PersonalInformations> personalInformations = personalInformationRepository.findByStudentId(id);
        if (personalInformations.isPresent())
            return personalInformationMapper.mapEntityToDTo(personalInformations.get());
        else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found id");

    }
}
