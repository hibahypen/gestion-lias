package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class MaterialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private LaboratoryMember member;        // Demandeur

    @Column(nullable = false)
    private String materialName;            // Ce dont il a besoin
    private Integer quantity = 1;

    @Column(length = 1500)
    private String justification;           // Justification scientifique ou technique

    private LocalDate requestDate;
    private LocalDate decisionDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.EN_ATTENTE;

    @ManyToOne
    @JoinColumn(name = "handled_by")
    private LaboratoryMember handledBy;     // Directeur ou Admin qui a traité la demande

    public enum RequestStatus {
        EN_ATTENTE, VALIDEE, REFUSEE
    }
}