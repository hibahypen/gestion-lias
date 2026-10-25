package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class MaterialAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private LaboratoryMember member;

    private LocalDate assignmentDate;       // Date où il a reçu le matériel
    private LocalDate expectedReturnDate;   // Date de retour prévue
    private LocalDate actualReturnDate;     // Date de restitution effective
    private boolean active = true;          // Toujours en sa possession ou rendu

    private String notes;
}