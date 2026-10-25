package ma.lias.gestion_lias.controller;

import ma.lias.gestion_lias.model.Team;
import ma.lias.gestion_lias.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("teamService", teamService);
        return "teams";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("team", new Team());
        return "team-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Team team) {
        teamService.save(team);
        return "redirect:/teams";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("team", teamService.getById(id));
        return "team-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        teamService.delete(id);
        return "redirect:/teams";
    }
}