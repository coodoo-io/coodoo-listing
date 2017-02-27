package io.coodoo.framework.listing.dbunit.model;

import org.junit.Ignore;
import org.junit.Test;

public class DatasetGenerationTest {


    private String[] strings =
                    {"Coodoo", "coodoo", "COODOO", "coo", "doo", "oo", "cooodooo", "doocoo", "o", "coodoo coodoo coodoo", "\t   coodoo\t\n  ", "", null};

    private Long[] longs = {null, -111L, -11L, -1L, 0L, 1L, 2L, 3L, 10L, 11L, 12L, 22L, 110L, 111L, 112L};

    private String[] dates = {"2017-01-01 00:00:00", "2017-12-31 23:59:59", "2016-01-01 00:00:00", "2016-12-31 23:59:59", "2015-01-01 00:00:00",
                    // "2015-12-31 23:59:59", "2014-01-01 00:00:00", "2014-12-31 23:59:59", "2017-01-10 12:00:00", "2017-01-20 12:00:00", "2017-01-30 12:00:00",
                    "2017-02-01 12:00:00", "2017-02-29 12:00:00", "2017-03-01 12:00:00", "2017-03-31 12:00:00", null};



    private String keyValue(String key, String value) {
        return " " + key + "='" + value + "'";
    }

    @Ignore
    @Test
    public void testGernerate() {

        int count = 0;

        StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>");
        sb.append(System.lineSeparator());
        sb.append("<dataset>");

        for (String string : strings) {
            for (Long longValue : longs) {
                for (TestEnum testEnum : TestEnum.values()) {
                    for (String dateString : dates) {
                        for (int i = 0; i < 3; i++) {

                            count++;

                            sb.append(System.lineSeparator());
                            sb.append("<TEST_ENTITY");
                            sb.append(keyValue("ID", Integer.valueOf(count).toString()));
                            // if (string != null) {
                            // sb.append(keyValue("STRING1", string));
                            // sb.append(keyValue("STRING_IGNORE", string));
                            // sb.append(keyValue("STRING2", string + testEnum.name()));
                            // }
                            // if (longValue != null) {
                            // sb.append(keyValue("LONG1", longValue.toString()));
                            // sb.append(keyValue("LONG2", longValue.toString()));
                            // sb.append(keyValue("LONG_LIKE", longValue.toString()));
                            // sb.append(keyValue("LONG_IGNORE", longValue.toString()));
                            // } else {
                            // sb.append(keyValue("LONG2", "0"));
                            // }
                            // sb.append(keyValue("ENUM1", testEnum.name()));
                            // if (i > 0) {
                            // sb.append(keyValue("ENUM2", TestEnum.values()[i].name()));
                            // }
                            // sb.append(keyValue("ENUM_IGNORE", testEnum.name()));
                            // switch (i) {
                            // case 1:
                            // sb.append(keyValue("BOOLEAN1", "true"));
                            // sb.append(keyValue("BOOLEAN2", "false"));
                            // sb.append(keyValue("BOOLEAN_IGNORE", "true"));
                            // break;
                            // case 2:
                            // sb.append(keyValue("BOOLEAN1", "false"));
                            // sb.append(keyValue("BOOLEAN2", "true"));
                            // sb.append(keyValue("BOOLEAN_IGNORE", "false"));
                            // break;
                            // case 3:
                            // sb.append(keyValue("BOOLEAN2", "true"));
                            // break;
                            // default:
                            // sb.append(keyValue("BOOLEAN2", "false"));
                            // break;
                            // }
                            // if (dateString != null) {
                            // sb.append(keyValue("DATE1", dateString));
                            // sb.append(keyValue("DATE2", dateString));
                            // sb.append(keyValue("DATE_IGNORE", dateString));
                            // }
                            sb.append(" />");
                        }
                    }
                }
            }
        }

        sb.append(System.lineSeparator());
        sb.append("</dataset>");
        sb.append(System.lineSeparator());

        System.out.println(sb.toString());

    }



}
