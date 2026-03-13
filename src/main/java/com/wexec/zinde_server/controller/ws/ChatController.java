package com.wexec.zinde_server.controller.ws;

import com.wexec.zinde_server.dto.request.SendMessageRequest;
import com.wexec.zinde_server.dto.response.ChatMessageResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.MessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessagingService messagingService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest req, Principal principal) {
        UserPrincipal userPrincipal = (UserPrincipal) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal).getPrincipal();
        messagingService.sendMessage(userPrincipal.getId(), req);
    }
}
