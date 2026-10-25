package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.service.HistoryService;
import ma.lias.gestion_lias.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final MemberService memberService;
    private final HistoryService historyService;

    @GetMapping
    public String view(Model model, Authentication auth) {
        LaboratoryMember member = memberService.getByEmail(auth.getName());
        model.addAttribute("member", member);
        model.addAttribute("history", historyService.getHistory(member.getId()));
        return "profile";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LaboratoryMember formMember,
                       @RequestParam(required = false) String newPassword,
                       Authentication auth) {
        memberService.updateOwnProfile(auth.getName(), formMember, newPassword);
        return "redirect:/profile?success";
    }
}