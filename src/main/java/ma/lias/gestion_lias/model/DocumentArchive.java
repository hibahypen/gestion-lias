package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class DocumentArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private DocumentType docType;

    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;

    private LocalDate documentDate;
    private LocalDate uploadedAt;

    // Structuration par événement (§10)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private ScientificEvent event;

    // Qui a uploadé (§21 traçabilité)
    @ManyToOne
    @JoinColumn(name = "uploaded_by_id")
    private LaboratoryMember uploadedBy;

    // Pour les PV de réunion (§17)
    private String reunionOrdreDuJour;

    public enum DocumentType {
        PV_REUNION,
        FINANCEMENT,
        PROGRAMME,
        ATTESTATION,
        RAPPORT,
        ADMINISTRATIF
    }
}