package ma.lias.gestion_lias.controller;

import ma.lias.gestion_lias.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final MemberService memberService;

    @GetMapping
    public String settings(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "settings";
    }
}