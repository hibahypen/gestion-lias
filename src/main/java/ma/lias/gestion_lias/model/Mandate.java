package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Mandate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private LaboratoryMember member;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String team;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active = true;
}