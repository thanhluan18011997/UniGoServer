package server.unigo.dataSeeding;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import server.unigo.dto.UsersDTO;
import server.unigo.repository.MoralRepository;
import server.unigo.repository.ScheduleRepository;
import server.unigo.repository.UserRepository;
import server.unigo.service.*;

import java.util.Base64;

@Log4j2
@Component
public class UpdateData {
    private final UserRepository userRepository;
    private final UserService userService;
    private final MoralService moralService;
    private final PersonalInformationService personalInformationService;
    private final ScheduleService scheduleService;
    private final StudyResultService studyResultService;
    private final TestService testService;
    private final FriendService friendService;
    private final CollaboratorService collaboratorService;
    private final ScheduleRepository scheduleRepository;


    @Autowired
    public UpdateData(UserRepository userRepository, UserService userService, MoralService moralService, MoralRepository moralRepository, PersonalInformationService personalInformationService, ScheduleService scheduleService, StudyResultService studyResultService, TestService testService, FriendService friendService, CollaboratorService collaboratorService, ScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.moralService = moralService;
        this.personalInformationService = personalInformationService;
        this.scheduleService = scheduleService;
        this.studyResultService = studyResultService;
        this.testService = testService;
        this.friendService = friendService;
        this.collaboratorService = collaboratorService;
        this.scheduleRepository = scheduleRepository;
    }

    @Async
//    @Scheduled(fixedRate = 1800000, initialDelay = 10000)
    @Scheduled(fixedRate = 600000, initialDelay = 1000)
    public void updatePersonalInformation() {
        System.out.println("update--------------------");
        log.info("update--------------------");
        userRepository.findAll().forEach(t ->
        {
            UsersDTO usersDTO = new UsersDTO();
            usersDTO.setUsername(t.getUsername());
            usersDTO.setPassword(new String(Base64.getDecoder().decode(t.getPassword())));
            if (userService.verifyUser(usersDTO).isStatus()) {
                personalInformationService.savePersonalInformation(t.getUsername());
            }


        });
    }

    @Async
//    @Scheduled(fixedRate = 1800000, initialDelay = 10000)
    @Scheduled(fixedRate = 600000, initialDelay = 40000)
    public void update() {
        System.out.println("update--------------------");
        log.info("update--------------------");
        userRepository.findAll().forEach(t ->
        {

            UsersDTO usersDTO = new UsersDTO();
            usersDTO.setUsername(t.getUsername());
            usersDTO.setPassword(new String(Base64.getDecoder().decode(t.getPassword())));
            if (userService.verifyUser(usersDTO).isStatus()&&!t.getUsername().equals("admin")) {
                friendService.saveFriend(t.getUsername());
                moralService.saveMoral(t.getUsername());
                scheduleService.saveSchedule(t.getUsername());
                studyResultService.saveStudentResult(t.getUsername());
                testService.saveTest(t.getUsername());
                scheduleRepository.findAll().forEach(c->collaboratorService.saveCollaborator(c.getId()));
            }


        });
    }


    @Async
//    @Scheduled(fixedRate = 1800000, initialDelay = 10000)
    @Scheduled(fixedRate = 60000000, initialDelay = 60000)
    public void updateCourse() {
        System.out.println("update--------------------");
        log.info("update--------------------");
        userRepository.findAll().forEach(t ->
        {

            UsersDTO usersDTO = new UsersDTO();
            usersDTO.setUsername(t.getUsername());
            usersDTO.setPassword(new String(Base64.getDecoder().decode(t.getPassword())));
            if (userService.verifyUser(usersDTO).isStatus()&&!t.getUsername().equals("admin")) {
                scheduleRepository.findAll().forEach(c->collaboratorService.saveCollaborator(c.getId()));
            }


        });
    }

}
