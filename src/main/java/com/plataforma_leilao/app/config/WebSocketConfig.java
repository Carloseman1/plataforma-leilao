package com.plataforma_leilao.app.config;

import com.plataforma_leilao.app.security.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Canal ao vivo do pregão.
 *
 * O broker é o simples, em memória: o que trafega aqui é o estado corrente de um
 * leilão, e a fonte da verdade continua sendo o banco — quem entra no meio do
 * pregão carrega o estado por REST e só então assina o canal.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    public WebSocketConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*");
    }

    /**
     * O handshake do WebSocket não passa pelo filtro de JWT das rotas /api, então
     * o token é conferido aqui, no CONNECT do STOMP. Sem isso qualquer um
     * assinaria o canal de qualquer leilão.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registro) {
        registro.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> mensagem, MessageChannel canal) {
                StompHeaderAccessor cabecalhos =
                        MessageHeaderAccessor.getAccessor(mensagem, StompHeaderAccessor.class);

                if (cabecalhos != null && StompCommand.CONNECT.equals(cabecalhos.getCommand())) {
                    exigirTokenValido(cabecalhos.getFirstNativeHeader("Authorization"));
                }

                return mensagem;
            }
        });
    }

    private void exigirTokenValido(String cabecalho) {
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token ausente na conexão do pregão.");
        }

        String token = cabecalho.substring("Bearer ".length()).trim();

        if (!jwtService.tokenValido(token)) {
            throw new IllegalArgumentException("Token inválido na conexão do pregão.");
        }
    }
}
