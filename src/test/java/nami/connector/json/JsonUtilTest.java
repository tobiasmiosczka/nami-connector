package nami.connector.json;

import nami.connector.namitypes.NamiStufe;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Test void testFromJsonWithValidAgeGroupShouldReturnAgeGroup() {
        //arrange
        String json = "\"Jungpfadfinder\"";

        //act
        NamiStufe result = JsonUtil.fromJson(json, NamiStufe.class);

        //assert
        assertEquals(NamiStufe.JUNGPFADFINDER, result);
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(100)
    public void testFromJsonShouldBeThreadSafe() throws InterruptedException {
        //arrange
        String json = "\"19.10.1993\"";
        ExecutorService service = Executors.newFixedThreadPool(10);
        List<Callable<LocalDate>> callables = IntStream.range(0, 1000)
                .mapToObj(i -> (Callable<LocalDate>) () -> JsonUtil.fromJson(json, LocalDate.class))
                .collect(Collectors.toList());

        //act
        List<LocalDate> localDates = service.invokeAll(callables).stream()
                .map(JsonUtilTest::tryGet)
                .collect(Collectors.toList());

        //assert
        for (LocalDate result : localDates) {
            assertNotNull(result);
            assertEquals(1993, result.getYear());
            assertEquals(10, result.getMonthValue());
            assertEquals(19, result.getDayOfMonth());
        }
    }

    private static <T> T tryGet(Future<T> future) {
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}