package server.unigo.service.serviceImp;

import org.mapstruct.factory.Mappers;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import server.unigo.dto.StudyTimesDTO;
import server.unigo.map.ScheduleMapper;
import server.unigo.dto.DetailSchedulesDTO;
import server.unigo.dto.SchedulesDTO;
import server.unigo.map.StudyTimeMapper;
import server.unigo.model.*;
import server.unigo.repository.*;
import server.unigo.service.ScheduleService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImp implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final DetailScheduleRepository detailScheduleRepository;
    private final RestTemplate restTemplate;
    private final WeeklySchedulesRepository weeklySchedulesRepository;
    private final PersonalInformationRepository personalInformationRepository;
    private final StudyTimeRepository studyTimeRepository;

    @Autowired
    public ScheduleServiceImp(ScheduleRepository scheduleRepository, DetailScheduleRepository detailScheduleRepository, RestTemplate restTemplate, WeeklySchedulesRepository weeklySchedulesRepository, PersonalInformationRepository personalInformationRepository, StudyTimeRepository studyTimeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.detailScheduleRepository = detailScheduleRepository;
        this.restTemplate = restTemplate;
        this.weeklySchedulesRepository = weeklySchedulesRepository;
        this.personalInformationRepository = personalInformationRepository;
        this.studyTimeRepository = studyTimeRepository;
    }

    @Override
    public void saveSchedule(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<List<SchedulesDTO>> entity = new HttpEntity<List<SchedulesDTO>>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://dnunigo.herokuapp.com/dut/")
                .queryParam("command", "get_schedule")
                .queryParam("session_id", id);
        ResponseEntity<List<SchedulesDTO>> responseEntity = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<SchedulesDTO>>() {
                });
        List<SchedulesDTO> schedulesDTOList = responseEntity.getBody();
        ModelMapper modelMapper = new ModelMapper();

        for (SchedulesDTO schedulesDTO : schedulesDTOList) {
            Set<DetailSchedules> detailSchedulesSet = new HashSet<>();
            for (DetailSchedulesDTO detailSchedulesDTO : schedulesDTO.getWeeklySchedule().getDetailSchedules()) {
                Set<StudyTimes> studyTimesSet = new HashSet<>();
                for (StudyTimesDTO studyTimesDTO : detailSchedulesDTO.getStudyTime()) {
                    StudyTimeMapper studyTimeMapper = Mappers.getMapper(StudyTimeMapper.class);
                    StudyTimes studyTimes = studyTimeMapper.mapDTOtoEntity(studyTimesDTO);
                    studyTimesSet.add(studyTimes);
                }
                DetailSchedules detailSchedules = modelMapper.map(detailSchedulesDTO, DetailSchedules.class);
                detailSchedules.setStudyTime(studyTimesSet);
                detailSchedulesSet.add(detailSchedules);
            }
            WeeklySchedules weeklySchedules = modelMapper.map(schedulesDTO.getWeeklySchedule(), WeeklySchedules.class);
            weeklySchedules.setDetailSchedules(detailSchedulesSet);
            ScheduleMapper scheduleMapper = Mappers.getMapper(ScheduleMapper.class);
            server.unigo.model.Schedules schedules = scheduleMapper.mapDTOtoEntity(schedulesDTO);
            PersonalInformations personalInformations = personalInformationRepository.findByStudentId(id).get();
            schedules.setPersonalInformation(personalInformations);
            Optional<Long> scheduleID = scheduleRepository.findScheduleIdByPersonalInformationAndCourseName(id, schedules.getCourseCode());
            if (scheduleID.isPresent())
                schedules.setId(scheduleID.get());
            Schedules schedules1 = scheduleRepository.save(schedules);
            Optional<Long> weeklySchedulesIdOptional = weeklySchedulesRepository.findIdBySchedulesId(schedules1.getId());
            if (scheduleID.isPresent()) {
                if (weeklySchedulesIdOptional.isPresent())
                    weeklySchedules.setId(weeklySchedulesIdOptional.get());
            }
            weeklySchedules.setSchedules(schedules1);
            WeeklySchedules weeklySchedules1 = weeklySchedulesRepository.save(weeklySchedules);
            Optional<Long> detailSchedulesIdOptional = detailScheduleRepository.findIdByWeeklyScheduleId(weeklySchedules1.getId());
            if (detailSchedulesIdOptional.isPresent()) {
                weeklySchedules1.getDetailSchedules().forEach(t -> {
                    t.getStudyTime().forEach(t1 -> {
                        studyTimeRepository.deleteIdByDetailSchedule(detailSchedulesIdOptional.get());
                    });
                });
                detailScheduleRepository.deleteByWeeklyScheduleId(weeklySchedules1.getId());
            }

            weeklySchedules1.getDetailSchedules().forEach(t -> {
                t.setWeeklySchedule(weeklySchedules1);
                detailScheduleRepository.save(t);
                t.getStudyTime().forEach(t1 -> {
                    t1.setDetailSchedule(t);
                    studyTimeRepository.save(t1);
                });
            });
            schedules1.setWeeklySchedules(weeklySchedules1);


        }


    }

    @Override
    public List<SchedulesDTO> getSchedule(String id) {
        ScheduleMapper scheduleMapper=Mappers.getMapper(ScheduleMapper.class);


        return scheduleRepository.findByPersonalInformationID(id).get().stream().map(t->scheduleMapper.mapEntityToDTo(t)).collect(Collectors.toList());
    }
}
