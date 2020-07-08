package org.informiz.auth;

import org.hyperledger.fabric.gateway.Wallet;
import org.informiz.repo.CryptoUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;

public class AuthUtils {

    public static Collection<GrantedAuthority> getUserAuthorities(String email) {
        // TODO: get wallet from encrypted storage based on user email address
        Wallet userWallet = CryptoUtils.getUserWallet(email);

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (userWallet == null) {
            return authorities; // No additional authorities
        }

        // All users have fact-checker permissions
        authorities.add(new SimpleGrantedAuthority("ROLE_CHECKER"));

        // TODO: get current channel name
        String channelId = "mychannel";
        if (CryptoUtils.isChannelMember(email, userWallet, channelId)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }

        if (CryptoUtils.isChannelAdmin(email, userWallet, channelId)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return authorities;
    }


    public static void getChannelProxy(String email, String channelId) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession userSession =  attr.getRequest().getSession(true);

        // TODO: if no proxy or channel changed - create proxy
        if (userSession.getAttribute(CryptoUtils.ChaincodeProxy.PROXY_ATTR) == null) {
            //CryptoUtils.ChaincodeProxy proxy = CryptoUtils.createChaincodeProxy(channelId, "informiz");
        }
        // TODO: update granted authorities if channel changes (trigger re-authentication?)
    }

}
