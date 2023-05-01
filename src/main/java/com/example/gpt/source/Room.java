package com.example.gpt.source;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.LinkedList;
import java.util.List;

public class Room {
    private final String currentRoom;
    private Integer tempMsgId = null;
    private final List<ChatMessage> history = new LinkedList<>();
    private int historySize;
    private Integer historyMaxSize = 3500;

    public Room (String currentRoom) {
        this.currentRoom = currentRoom;
    }

    public Integer getTempMsgIdAndClear() {
        if (tempMsgId != null) {
            int tempMsgIdCopy = tempMsgId;
            tempMsgId = null;
            return tempMsgIdCopy;
        } else {
            return null;
        }
    }

    public void setTempMsgId (Integer tempMsgId) {
        this.tempMsgId = tempMsgId;
    }

    public boolean isTempMsgExist () {
        return tempMsgId != null;
    }

    public List<ChatMessage> getMessages() {
        return history;
    }

    public void addMsgToChat (ChatMessage lastMsg) {
        history.add(lastMsg);
        historySize = historySize + lastMsg.getContent().length();
        while (historySize > historyMaxSize) {
            historySize = historySize - history.get(0).getContent().length();
            history.remove(0);
        }
    }

    public String getCurrentRoom() {
        return currentRoom;
    }
}
