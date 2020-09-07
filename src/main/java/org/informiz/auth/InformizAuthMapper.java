package org.informiz.auth;

import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class InformizAuthMapper implements GrantedAuthoritiesMapper {

    @Autowired
    private FactCheckerRepository factCheckerRepo;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        authorities.forEach(authority -> {
            if (OAuth2UserAuthority.class.isInstance(authority)) {
                OAuth2UserAuthority oidcUserAuthority = (OAuth2UserAuthority) authority;
                String email = oidcUserAuthority.getAttributes().get("email").toString();
                AuthUtils.getUserAuthorities(email,
                        // TODO: may not be a member, get user from ES
                        factCheckerRepo.findByEmail(email).getEntityId())
                        .forEach(auth -> mappedAuthorities.add(auth));
            }
            mappedAuthorities.add(authority);
        });

        return mappedAuthorities;
    }
}
