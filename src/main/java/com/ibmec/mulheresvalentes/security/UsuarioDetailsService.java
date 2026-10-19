package com.ibmec.mulheresvalentes.security;

import com.ibmec.mulheresvalentes.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        String normalizado = login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
        return usuarioRepository.findByLogin(normalizado)
                .map(UsuarioAutenticado::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário ou senha incorretos."));
    }
}
