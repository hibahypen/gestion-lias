package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class AdhesionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    private String teamWanted;
    private String establishmentOrigin;

    @Column(length = 2000)
    private String motivationLetter;

    private String cvUrl;
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    private AdhesionStatus status = AdhesionStatus.EN_ATTENTE;

    private LocalDate dateSubmission;
    private LocalDate dateDecision;

    @ManyToOne
    @JoinColumn(name = "traite_par_id")
    private LaboratoryMember traitePar;

    public enum AdhesionStatus {
        EN_ATTENTE, ACCEPTEE, REFUSEE
    }
}