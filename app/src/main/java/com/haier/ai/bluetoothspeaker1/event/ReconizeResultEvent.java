package com.haier.ai.bluetoothspeaker1.event;

/**
 * author: qu
 * date: 16-9-28
 * introduce: 识别结果
 */

public class ReconizeResultEvent {

    public final String message;

    public ReconizeResultEvent(String msg){
        this.message = msg;
    }
}
