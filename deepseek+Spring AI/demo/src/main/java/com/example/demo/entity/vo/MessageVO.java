package com.example.demo.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

@NoArgsConstructor
@Data
public class MessageVO {
    private String role;
    private String content;

    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case SYSTEM:
                role = "system";
                content = message.getText();
                break;
            case USER:
                role = "user";
                content = message.getText();
                break;
            case ASSISTANT:
                role = "assistant";
                content = message.getText();
                break;
            case TOOL:
                role = "tool";
                content = message.getText();
        }
        this.role = role;
        this.content = message.getText();
    }
}
