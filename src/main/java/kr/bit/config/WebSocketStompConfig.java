package kr.bit.config;

import kr.bit.interceptor.HttpSessionIdHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("소켓 서버 열림");
        // 클라이언트가 연결할 엔드포인트. SockJS를 사용하여 폴백 기능 제공.
        registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS()
                .setInterceptors(new HttpSessionIdHandshakeInterceptor());;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration channelRegistration) {

    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration channelRegistration) {

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {

    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {
        converters.add(new StringMessageConverter());
        converters.add(new MappingJackson2MessageConverter());
        return true;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 전송할 메시지의 접두사 설정 (서버의 @MessageMapping 핸들러와 매칭됨)
        registry.setApplicationDestinationPrefixes("/blindtime");

        // 클라이언트에게 전달할 메시지를 브로드캐스트할 때 사용할 주제(prefix) 설정
        registry.enableSimpleBroker("/topic", "/queue");
    }
}
