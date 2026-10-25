package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;              // Ex: OCP Group, CNRS, Université Hassan II, Microsoft Research

    @Enumerated(EnumType.STRING)
    private PartnerType type;         // ACADEMIQUE, INDUSTRIEL, INSTITUTIONNEL, INTERNATIONAL

    private String country;           // Maroc, France, USA...
    private String city;
    private String contactPerson;     // Responsable du partenariat
    private String contactEmail;
    private String contactPhone;

    @Column(length = 1000)
    private String description;

    public enum PartnerType {
        ACADEMIQUE, INDUSTRIEL, INSTITUTIONNEL, INTERNATIONAL
    }
}