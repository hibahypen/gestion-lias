package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.InternalMessage;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.repository.InternalMessageRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.service.TeamService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/communication")
@RequiredArgsConstructor
public class CommunicationController {

    private final InternalMessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final TeamService teamService;
    private final EventRepository eventRepository;

    @GetMapping
    public String index(@RequestParam(defaultValue = "GENERAL") String channelType,
                        @RequestParam(required = false) String team,
                        @RequestParam(required = false) Long eventId,
                        Model model, Authentication auth) {

        LaboratoryMember current = memberRepository.findByEmail(auth.getName()).orElseThrow();

        if ("EQUIPE".equals(channelType) && (team == null || team.isBlank())) {
            team = current.getTeam() != null ? current.getTeam() : "Data Science & Intelligence Artificielle";
        }

        if ("EQUIPE".equals(channelType)) {
            model.addAttribute("messages", messageRepository.findByChannelTypeAndTargetTeamOrderBySentAtDesc("EQUIPE", team));
        } else if ("EVENEMENT".equals(channelType) && eventId != null) {
            model.addAttribute("messages", messageRepository.findByChannelTypeAndTargetEventIdOrderBySentAtDesc("EVENEMENT", eventId));
        } else {
            model.addAttribute("messages", messageRepository.findByChannelTypeOrderBySentAtDesc("GENERAL"));
        }

        model.addAttribute("channelType", channelType);
        model.addAttribute("currentTeam", team);
        model.addAttribute("currentEventId", eventId);
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("events", eventRepository.findAll());
        model.addAttribute("currentUser", current);

        return "communication";
    }

    @PostMapping("/post")
    public String postMessage(@RequestParam String content,
                              @RequestParam String channelType,
                              @RequestParam(required = false) String targetTeam,
                              @RequestParam(required = false) Long targetEventId,
                              Authentication auth) {

        LaboratoryMember sender = memberRepository.findByEmail(auth.getName()).orElseThrow();
        InternalMessage msg = new InternalMessage();
        msg.setContent(content);
        msg.setSender(sender);
        msg.setSentAt(LocalDateTime.now());
        msg.setChannelType(channelType);
        msg.setTargetTeam(targetTeam);

        if (targetEventId != null) {
            eventRepository.findById(targetEventId).ifPresent(msg::setTargetEvent);
        }

        messageRepository.save(msg);

        String redirectUrl = "redirect:/communication?channelType=" + channelType;
        if ("EQUIPE".equals(channelType) && targetTeam != null) {
            redirectUrl += "&team=" + targetTeam;
        } else if ("EVENEMENT".equals(channelType) && targetEventId != null) {
            redirectUrl += "&eventId=" + targetEventId;
        }
        return redirectUrl;
    }
}