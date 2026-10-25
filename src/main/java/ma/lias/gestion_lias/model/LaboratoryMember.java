package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class LaboratoryMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    // Statut : nature de l'appartenance au labo (cahier de charge §21)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    // Rôle : responsabilité de gouvernance, séparé du statut (cahier de charge §21)
    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBRE;

    private String photoUrl;
    private String telephone;
    private String biography;
    private String centresInteret;
    private LocalDate dateAffiliation;
    private LocalDate dateNaissance;
    private LocalDate dateEmbauche;
    private String establishmentOrigin;
    private String laboratoireOrigine;
    private String specialite;
    private String team;

    // Activation manuelle gérée par l'admin (en plus du gel/désactivation auto liés au statut)
    private boolean active = true;

    public enum MemberStatus {
        PERMANENT, ASSOCIE, DOCTORANT, RETRAITE, ANCIEN
    }
}