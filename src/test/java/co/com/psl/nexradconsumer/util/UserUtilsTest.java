package co.com.psl.nexradconsumer.util;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Created by acastanedav on 22/12/16.
 */
public class UserUtilsTest {

    TestUtils testUtils = new TestUtils();

    @Test
    public void whenTextShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<LocalDate> date = userUtils.getDate("aaaa");

        Assert.assertFalse(date.isPresent());
        verify(printStream, times(1)).println(Mockito.anyString());
    }

    @Test
    public void whenStringSeparatedBySlashShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<LocalDate> date = userUtils.getDate("a/a/a");

        Assert.assertFalse(date.isPresent());
        verify(printStream, times(1)).println(Mockito.anyString());
    }

    @Test
    public void whenInvalidFormatShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<LocalDate> date = userUtils.getDate("32/1/1/2016");

        Assert.assertFalse(date.isPresent());
        verify(printStream, times(1)).println(Mockito.anyString());
    }

    @Test
    public void whenInvalidDayShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<LocalDate> date = userUtils.getDate("40/5/2016");
        Assert.assertFalse(date.isPresent());

        Assert.assertFalse(date.isPresent());
        verify(printStream, times(1)).println(Mockito.anyString());
    }

    @Test
    public void whenInvalidMonthShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<LocalDate> date = userUtils.getDate("1/13/2016");
        Assert.assertFalse(date.isPresent());

        Assert.assertFalse(date.isPresent());
        verify(printStream, times(1)).println(Mockito.anyString());
    }

    @Test
    public void whenYearBeforeLimitShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<LocalDate> date = userUtils.getDate("1/1/1989");

        Assert.assertFalse(date.isPresent());
        verify(printStream, never()).println(Mockito.anyString());
    }

    @Test
    public void whenDateAfterTodayShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        LocalDate entry = Instant.ofEpochMilli(System.currentTimeMillis() + 86400000).atZone(ZoneId.systemDefault()).toLocalDate();

        Optional<LocalDate> date = userUtils.getDate(entry.getDayOfMonth()
                + "/" + entry.getMonthValue()
                + "/" + entry.getYear());

        Assert.assertFalse(date.isPresent());
        verify(printStream, never()).println(Mockito.anyString());
    }

    @Test
    public void whenValidDateShouldReturnADate() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        LocalDate entry = Instant.ofEpochMilli(System.currentTimeMillis() - 86400000).atZone(ZoneId.systemDefault()).toLocalDate();

        Optional<LocalDate> date = userUtils.getDate(entry.getDayOfMonth()
                + "/" + entry.getMonthValue()
                + "/" + entry.getYear());

        Assert.assertTrue(date.isPresent());
        Assert.assertEquals(entry.getYear(),date.get().getYear());
        Assert.assertEquals(entry.getMonthValue(),date.get().getMonthValue());
        Assert.assertEquals(entry.getDayOfMonth(),date.get().getDayOfMonth());
        verify(printStream, never()).println(Mockito.anyString());
    }

    @Test
    public void whenTwoShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<Integer> result = userUtils.getNumber("2", testUtils.random.nextInt(20));

        Assert.assertFalse(result.isPresent());
        verify(printStream, never()).println(Mockito.anyString());
    }

    @Test
    public void whenNumberOverSizeShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        int size = testUtils.random.nextInt(20);
        Optional<Integer> result = userUtils.getNumber(size + "", size);

        Assert.assertFalse(result.isPresent());
        verify(printStream, never()).println(Mockito.anyString());
    }

    @Test
    public void whenNotANumberShouldReturnEmptyOptional() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        Optional<Integer> result = userUtils.getNumber("hello world", testUtils.random.nextInt(20));

        Assert.assertFalse(result.isPresent());
        verify(printStream, times(1)).println(Mockito.anyString());
    }

    @Test
    public void whenAValidNumberReturnANumber() {
        UserUtils userUtils = new UserUtils();
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setPrintStream(printStream);

        int size = 30;
        int index = testUtils.random.nextInt(25) + 3;
        Optional<Integer> result = userUtils.getNumber(index + "", size);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(Integer.valueOf(index), result.get());
        verify(printStream, never()).println(Mockito.anyString());
    }

    @Test(expected = IOException.class)
    public void whenInputStreamThrowExceptionThenThrowExceptionForDate() throws IOException {
        UserUtils userUtils = new UserUtils();
        InputStream inputStream = mock(InputStream.class);
        userUtils.setInputStream(inputStream);
        when(inputStream.read()).thenThrow(new IOException());

        userUtils.getDateFromUser();
    }

    @Test(expected = IOException.class)
    public void whenInputStreamThrowExceptionThenThrowExceptionForNumber() throws IOException {
        UserUtils userUtils = new UserUtils();
        InputStream inputStream = mock(InputStream.class);
        userUtils.setInputStream(inputStream);
        when(inputStream.read()).thenThrow(new IOException());

        userUtils.getIndexFromUser(testUtils.random.nextInt(20));
    }

    @Test
    public void whenValidStringForDateShouldReturnDate() throws IOException {
        UserUtils userUtils = new UserUtils();

        LocalDate entry = Instant.ofEpochMilli(System.currentTimeMillis() - 86400000).atZone(ZoneId.systemDefault()).toLocalDate();
        InputStream inputStream = IOUtils.toInputStream(entry.getDayOfMonth()
                + "/" + entry.getMonthValue()
                + "/" + entry.getYear());
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setInputStream(inputStream);
        userUtils.setPrintStream(printStream);

        LocalDate date = userUtils.getDateFromUser();

        Assert.assertEquals(entry.getYear(),date.getYear());
        Assert.assertEquals(entry.getMonthValue(),date.getMonthValue());
        Assert.assertEquals(entry.getDayOfMonth(),date.getDayOfMonth());
        verify(printStream, times(1)).println("Please enter a date between 01/06/1991 and today, keep in mind format dd/mm/yyyy");
    }

    @Test
    public void whenValidNumberShouldReturnNumber() throws IOException {
        UserUtils userUtils = new UserUtils();

        int size = 30;
        int index = testUtils.random.nextInt(25) + 3;
        InputStream inputStream = IOUtils.toInputStream(index + "");
        PrintStream printStream = mock(PrintStream.class);
        userUtils.setInputStream(inputStream);
        userUtils.setPrintStream(printStream);

        int result = userUtils.getIndexFromUser(size);

        Assert.assertEquals(index, result);
        verify(printStream, times(1)).println("This station has " + size + " files, ¿how many files do you want to work with?, should be higher than 2");
    }
}
