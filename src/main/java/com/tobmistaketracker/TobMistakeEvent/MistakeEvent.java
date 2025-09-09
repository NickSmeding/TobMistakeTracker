package com.tobmistaketracker.TobMistakeEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

@Getter
public class MistakeEvent {
    @Getter
    private final TobMistake mistake;
    @Nullable
    private final String customMessage;


    public MistakeEvent(TobMistake mistake) {
        this(mistake, null);
    }

    public MistakeEvent(TobMistake mistake, @Nullable String customMessage) {
        this.mistake = mistake;
        this.customMessage = customMessage;
    }


    public String getMessage() {
        return customMessage != null ? customMessage : mistake.getChatMessage();
    }

    public String getMistakeName() {
        return mistake.getMistakeName();
    }

    public BufferedImage getMistakeImage() {
        return mistake.getMistakeImage();
    }
}
