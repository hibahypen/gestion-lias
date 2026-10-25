package ma.lias.gestion_lias.service;

import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.Mandate;
import ma.lias.gestion_lias.model.Role;
import ma.lias.gestion_lias.model.Team;
import ma.lias.gestion_lias.repository.MandateRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final MandateRepository mandateRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getById(Long id) {
        return teamRepository.findById(id).orElseThrow();
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

    public void delete(Long id) {
        teamRepository.deleteById(id);
    }

    public List<LaboratoryMember> getMembersOfTeam(String teamName) {
        return memberRepository.findAll().stream()
                .filter(m -> teamName != null && teamName.equals(m.getTeam()))
                .toList();
    }

    public long countMembers(String teamName) {
        return getMembersOfTeam(teamName).size();
    }

    public String getChefNom(String teamName) {
        return mandateRepository.findByRoleAndTeamAndActiveTrue(Role.CHEF_EQUIPE, teamName)
                .map(Mandate::getMember)
                .map(m -> m.getFirstName() + " " + m.getLastName())
                .orElse("Non assigné");
    }
}