package com.example.gpt.source;

import static com.example.gpt.source.Commands.*;

public class MessageTexts {
    public static final String INTRODUCTION =
            COMMAND_HI_GPT + "     - to start chatting with ChatGPT 3.5.%n" +
            COMMAND_ASK_GPT + "     - to ask question ChatGPT 3.%n" +
            COMMAND_NEW_PIC + "    - to create a picture by description. Using DALL-E AI.%n%n" +
            COMMAND_START + "     - move to the main menu.";
    public static final String START_CONVERSATION = "Hello, %s!%nI am ChatGPT 3 what do you want to talk about?";
    public static final String START_CHATTING = "Hi, %s!%nIf you wish to start a new conversation, just tap on this " +
            "menu item once more and I will forget our previous topic.%nWhat do you want to talk about?";
    public static final String START_DRAWING = "Hello, %s!%nWhat do you want to draw?";
    public static final String ERROR_MSG = "Oops.. Some problems with chat:%n%s";
    public static final String WAITING_MSG = "Hmm...";
    public static final String ABOUT =
            "Author - Ilya Kuchaev%n" +
            "Contacts:%n" +
            "https://t.me/IlyaKIA03%n" +
            "https://github.com/IlyaKIA%n" +
            "https://www.linkedin.com/in/kuchaev-ilya";
    public static final String LIMIT_MSG = "Sorry, but your daily limit is exhausted";
    public static final String START_AVATAR = "Sorry, but your daily limit is exhausted";
}
