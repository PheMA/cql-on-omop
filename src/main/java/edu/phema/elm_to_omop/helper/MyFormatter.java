package edu.phema.elm_to_omop.helper;

import java.util.logging.*;

public class MyFormatter extends Formatter{

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getLevel()).append(':');
        sb.append(record.getMessage()).append('\n');
        return sb.toString();
    }    
}