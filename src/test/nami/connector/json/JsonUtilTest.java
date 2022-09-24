package nami.connector.json;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    public void testFromJsonWithValidDateTimeShouldReturnDate() {
        //arrange
        String json = "\"1993-10-19 12:34:56\"";

        //act
        LocalDateTime result = JsonUtil.fromJson(json, LocalDateTime.class);

        //assert
        assertNotNull(result);
        assertEquals(1993, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(19, result.getDayOfMonth());
        assertEquals(12, result.getHour());
        assertEquals(34, result.getMinute());
        assertEquals(56, result.getSecond());
    }

    @Test
    public void testFromJsonWithValidDateShouldReturnDate() {
        //arrange
        String json = "\"19.10.1993\"";

        //act
        LocalDate result = JsonUtil.fromJson(json, LocalDate.class);

        //assert
        assertNotNull(result);
        assertEquals(1993, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(19, result.getDayOfMonth());
    }
}