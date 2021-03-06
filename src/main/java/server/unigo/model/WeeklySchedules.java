




package server.unigo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import server.unigo.dto.SchedulesDTO;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class WeeklySchedules extends BaseEntity {
    String raw;
    @OneToMany(mappedBy ="weeklySchedule",cascade = CascadeType.ALL)
    Set<DetailSchedules> detailSchedules;
    @OneToOne
    Schedules schedules;
}