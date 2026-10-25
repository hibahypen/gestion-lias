package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String pubType;
    private Integer pubYear;
    private String journal;
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "added_by")
    private LaboratoryMember addedBy;
}