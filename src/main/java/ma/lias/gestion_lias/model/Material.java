package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;              // Ex: PC Portable Dell XPS, Serveur GPU RTX 4090

    private String category;          // Informatique, Audiovisuel, Réseau, Bureau...
    private String serialNumber;      // N° de série ou Code inventaire
    private Integer quantity = 1;     // Stock disponible
    private LocalDate arrivalDate;    // Date d'arrivage (§14)

    @Enumerated(EnumType.STRING)
    private MaterialState state = MaterialState.NEUF;

    @Column(length = 1000)
    private String description;

    public enum MaterialState {
        NEUF, BON, USAGE, EN_PANNE
    }
}