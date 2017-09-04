package com.github.mendess2526.discordbot;


import java.text.SimpleDateFormat;
import java.util.Date;


@SuppressWarnings("WeakerAccess")
public class LoggerService {
    static final int INFO = 1;
    static final int ERROR = 2;
    static final int SUCC = 3;

    public static void log(String message, int type){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        switch (type) {
            case LoggerService.INFO:
                message = " INFO - " + message;
                break;
            case LoggerService.ERROR:
                message = " ERROR - " + message;
                break;
            case LoggerService.SUCC:
                message = " SUCC - " + message;
                break;
            default:
                message = " XXX - " + message;
                break;
        }
        message = sdf.format(new Date()) + message;
        logToConsole(message,type);
    }

    private static void logToConsole(String message, int type) {
        final String ANSI_RESET = " \u001B[0m ";
        final String ANSI_RED = " \u001B[31m ";
        final String ANSI_GREEN = " \u001B[32m ";
        final String ANSI_CYAN = " \u001B[36m ";

        switch (type) {
            case LoggerService.INFO:
                message = ANSI_CYAN + message + ANSI_RESET;
                break;
            case LoggerService.ERROR:
                message = ANSI_RED  + message + ANSI_RESET;
                break;
            case LoggerService.SUCC:
                message = ANSI_GREEN + message + ANSI_RESET;
                break;
            default:
                break;
        }
        System.out.println(message);
    }
}
