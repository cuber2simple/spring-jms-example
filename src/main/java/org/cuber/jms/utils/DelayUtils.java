package org.cuber.jms.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.regex.Matcher;

import static org.cuber.jms.constants.MqConstants.PATTERN;


public class DelayUtils {

    public static boolean isRight(String sequence) {
        boolean result = false;
        if (StringUtils.isNotBlank(sequence)) {
            String[] durations = sequence.split(",");
            result = Arrays.stream(durations).allMatch(DelayUtils::match);
        }
        return result;
    }

    public static Duration duration(String sequence, int current) {
        String[] durations = sequence.split(",");
        Duration duration = Duration.parse(StringUtils.trim(durations[current]));
        return duration;
    }


    private static boolean match(String text) {
        Matcher matcher = PATTERN.matcher(StringUtils.trim(text));
        return matcher.matches();
    }
}
