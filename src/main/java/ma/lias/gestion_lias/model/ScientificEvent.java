package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
public class ScientificEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private LocalDate eventDate;
    private String description;
    private boolean archived = false;

    // Cahier de charge §9 : affectation d'organisateurs (plusieurs possibles)
    @ManyToMany
    @JoinTable(name = "event_organizers",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"))
    private List<LaboratoryMember> organizers = new ArrayList<>();

    public String getOrganizersLabel() {
        if (organizers == null || organizers.isEmpty()) return "";
        return organizers.stream()
                .map(m -> m.getFirstName() + " " + m.getLastName())
                .collect(Collectors.joining(", "));
    }

    public enum EventType {
        CONFERENCE, SEMINAIRE, WORKSHOP
    }
}