
package ma.lias.gestion_lias.config;

import ma.lias.gestion_lias.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/style.css", "/css/**", "/js/**", "/images/**", "/*.css",
                                "/equipes", "/activites", "/publications-publiques",
                                "/adhesion", "/adhesion/save", "/adhesion-merci"
                        ).permitAll()
                        .requestMatchers("/dashboard", "/profile/**", "/publications/**").authenticated()
                        .requestMatchers(request -> {
                            var current = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                            return current != null && current.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("STATUT_DOCTORANT"));
                        }).denyAll()
                        .requestMatchers("/events/new", "/events/save", "/events/edit/**", "/events/delete/**",
                                "/documents/delete/**", "/materials/edit/**")
                        .hasAnyRole("ADMIN", "DIRECTEUR")
                        .requestMatchers("/settings/**").hasRole("ADMIN")
                        .requestMatchers("/members/new", "/members/save", "/members/delete/**", "/members/toggle/**", "/members/edit/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/adhesion-requests/**").hasAnyRole("DIRECTEUR", "ADMIN")
                        .requestMatchers("/mandates/**").hasAnyRole("DIRECTEUR", "ADMIN")
                        .requestMatchers("/reports/**").hasAnyRole("DIRECTEUR", "VICE_DIRECTEUR", "ADMIN")
                        .requestMatchers("/teams/new", "/teams/save", "/teams/delete/**", "/teams/edit/**")
                        .hasAnyRole("DIRECTEUR", "ADMIN")

                        .requestMatchers("/materials/requests/new", "/materials/requests/save").authenticated()
                        .requestMatchers("/materials/requests/accept/**", "/materials/requests/refuse/**").hasAnyRole("DIRECTEUR", "ADMIN")
                        .requestMatchers("/materials/new", "/materials/save", "/materials/delete/**", "/materials/assign", "/materials/return/**").hasAnyRole("DIRECTEUR", "ADMIN")
                        .requestMatchers("/materials/**").authenticated()

                        .requestMatchers("/conventions/new", "/conventions/save", "/conventions/delete/**", "/conventions/edit/**", "/conventions/partners/**").hasAnyRole("DIRECTEUR", "ADMIN")
                        .requestMatchers("/conventions", "/conventions/view/**").authenticated()

                        // Cahier de charge §2.2 : Doctorant = aucun accès aux modules internes (sauf publications),
                        // Associé = accès limité (pas de gestion membres/équipes). On bloque la consultation
                        // des listes internes pour ces 2 statuts, même via URL directe.
                        .requestMatchers("/members", "/members/view/**", "/teams")
                        .access((authentication, context) -> new AuthorizationDecision(
                                authentication.get().isAuthenticated()
                                && !(authentication.get() instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)
                                && authentication.get().getAuthorities().stream().noneMatch(a ->
                                        a.getAuthority().equals("STATUT_DOCTORANT") || a.getAuthority().equals("STATUT_ASSOCIE"))
                        ))
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }
}
