package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class MemberHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private LaboratoryMember member;

    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    private String fieldName;

    @Column(length = 2000)
    private String oldValue;

    @Column(length = 2000)
    private String newValue;

    private String changedBy;
    private LocalDateTime changedAt;

    public enum ChangeType {
        PROFIL, STATUT, ROLE, AFFILIATION, COMPTE
    }
}