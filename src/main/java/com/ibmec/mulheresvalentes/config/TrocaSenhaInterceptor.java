package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.repository.UsuarioRepository;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.security.UsuarioLogado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Para cada página do painel, confere a conta da pessoa logada:
 * <ul>
 *   <li>se ela foi desativada, encerra a sessão;</li>
 *   <li>se está com senha temporária, leva para a tela de troca de senha.</li>
 * </ul>
 */
@Component
public class TrocaSenhaInterceptor implements HandlerInterceptor {

    static final String PAGINA_TROCA = "/minha-conta/senha";

    private final UsuarioRepository usuarioRepository;

    public TrocaSenhaInterceptor(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Optional<UsuarioAutenticado> logado = UsuarioLogado.atual();
        if (logado.isEmpty()) {
            return true;
        }
        Optional<Usuario> usuario = usuarioRepository.findById(logado.get().getId());
        if (usuario.isEmpty() || !usuario.get().isAtivo()) {
            SecurityContextHolder.clearContext();
            HttpSession sessao = request.getSession(false);
            if (sessao != null) {
                sessao.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login?desativado");
            return false;
        }
        if (usuario.get().isDeveTrocarSenha()) {
            response.sendRedirect(request.getContextPath() + PAGINA_TROCA);
            return false;
        }
        return true;
    }
}
