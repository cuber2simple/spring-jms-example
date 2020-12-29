package org.cuber.jms.constants;

import java.util.regex.Pattern;

public class MqConstants {
    /**
     * 延迟的系列
     */
    public static final String DELAY_SEQUENCE = "X-DELAY-SEQUENCE";
    /**
     * 当前执行的次数
     */
    public static final String DELAY_SEQUENCE_CURRENT = "X-DELAY-SEQUENCE-CURRENT";

    public static final String DEAD_CALL =  "DEAD_CALL";

    public static final String DELAY_EXCHANGE_TYPE = "x-delayed-message";

    public static final String DELAY_TYPE = "x-delayed-type";

    public static final String DELAY_EXCHANGE = "x-delay-exchange";

    public static final String _DEAD_CALL =  "_DEAD_CALL";

    public static final String TYPE_PROPERTY = "_type";

    public static final Pattern PATTERN =
            Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)D)?" +
                            "(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?",
                    Pattern.CASE_INSENSITIVE);



}
