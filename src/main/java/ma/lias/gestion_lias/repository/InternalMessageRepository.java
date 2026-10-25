package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.InternalMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InternalMessageRepository extends JpaRepository<InternalMessage, Long> {
    List<InternalMessage> findByChannelTypeOrderBySentAtDesc(String channelType);
    List<InternalMessage> findByChannelTypeAndTargetTeamOrderBySentAtDesc(String channelType, String targetTeam);
    List<InternalMessage> findByChannelTypeAndTargetEventIdOrderBySentAtDesc(String channelType, Long eventId);
}