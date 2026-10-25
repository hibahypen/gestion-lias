package ma.lias.gestion_lias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class InternalMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String content;

    private LocalDateTime sentAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private LaboratoryMember sender;

    // القناة: إما خاصة بفريق، أو بفعالية، أو نقاش عام بالمختبر
    private String channelType; // "GENERAL", "EQUIPE", "EVENEMENT"

    private String targetTeam;  // اسم الفريق إذا كان channelType = "EQUIPE"

    @ManyToOne
    @JoinColumn(name = "target_event_id")
    private ScientificEvent targetEvent; // الفعالية إذا كان channelType = "EVENEMENT"
}