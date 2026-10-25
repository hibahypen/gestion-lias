package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Convention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;                 // Ex: Convention R&D Big Data & IA pour l'Agriculture

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;              // Partenaire contractuel

    private LocalDate signatureDate;
    private LocalDate startDate;
    private LocalDate endDate;

    private Double budget;                // Financement / Budget alloué (DH)

    @Enumerated(EnumType.STRING)
    private ConventionStatus status = ConventionStatus.ACTIVE;

    @Column(length = 2000)
    private String objectives;            // Objectifs scientifiques et techniques

    private String documentUrl;           // Lien ou nom du document signé archivé

    // Suivi des activités associées (§16) : conférences/workshops liés à cette convention
    @ManyToMany
    @JoinTable(
            name = "convention_events",
            joinColumns = @JoinColumn(name = "convention_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<ScientificEvent> associatedEvents = new ArrayList<>();

    public enum ConventionStatus {
        ACTIVE, EXPIREE, EN_NEGOCIATION, RESILIEE
    }
}