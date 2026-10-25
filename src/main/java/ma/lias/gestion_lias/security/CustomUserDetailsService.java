package ma.lias.gestion_lias.security;

import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LaboratoryMember member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun compte trouvé pour : " + email));

        boolean locked = member.getStatus() == LaboratoryMember.MemberStatus.RETRAITE;
        boolean disabled = !member.isActive() || member.getStatus() == LaboratoryMember.MemberStatus.ANCIEN;

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities("ROLE_" + member.getRole().name(), "STATUT_" + member.getStatus().name())
                .accountLocked(locked)
                .disabled(disabled)
                .build();
    }
}