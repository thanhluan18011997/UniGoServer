package server.unigo.service.serviceImp;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import server.unigo.dto.NotificationsDTO;
import server.unigo.dto.UsersDTO;
import server.unigo.map.NotificationMapper;
import server.unigo.model.Notifications;
import server.unigo.repository.NotificationRepository;
import server.unigo.repository.UserRepository;
import server.unigo.service.NotificationService;
import server.unigo.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImp implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate;
    private final UserService userService;

    @Autowired
    public NotificationServiceImp(NotificationRepository notificationRepository, RestTemplate restTemplate, UserRepository userRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.restTemplate = restTemplate;
        this.userService = userService;
    }
    //when using generic, appear cast error :" return hash map instead of json "

    @Override
    public void saveNotification() {
        UsersDTO usersDTO=new UsersDTO();
        usersDTO.setUsername("102170100");
        usersDTO.setPassword("luan102170100");
        userService.verifyUser(usersDTO);
        notificationRepository.deleteAll();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<List<NotificationsDTO>> entity = new HttpEntity<List<NotificationsDTO>>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://dnunigo.herokuapp.com/dut/")
                .queryParam("command", "get_class_noti")
                .queryParam("session_id", "102170100");
        ResponseEntity<List<NotificationsDTO>> responseEntity = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<NotificationsDTO>>() {
                });
        List<NotificationsDTO> notificationsDTOList = responseEntity.getBody();
//        ModelMapper modelMapper = new ModelMapper();
        NotificationMapper notificationMapper = Mappers.getMapper(NotificationMapper.class);
        List<Notifications> notificationsList = notificationsDTOList.stream()
                .map(t -> notificationMapper.mapDTOtoEntity(t)).collect(Collectors.toList());
        notificationsList.forEach(t -> notificationRepository.save(t));
    }

    public List<NotificationsDTO> getNotification() {

        NotificationMapper notificationMapper = Mappers.getMapper(NotificationMapper.class);
        List<NotificationsDTO> notificationsDTOList = notificationRepository.findAll().stream().map(t ->
                notificationMapper.mapEntityToDTo(t)
        ).collect(Collectors.toList());
        return notificationsDTOList;
    }
}
