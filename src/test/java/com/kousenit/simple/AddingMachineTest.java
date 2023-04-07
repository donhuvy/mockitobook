package com.kousenit.simple;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AddingMachineTest {

    @Test
    public void getTotalUsingLoop() {
        // Create a stubbed list
        // Use no-arg version of mock() to avoid
        // compiler warning about unchecked cast
        List<Integer> mockList = mock();

        // Set the expectations on the stub
        when(mockList.size()).thenReturn(3);
        when(mockList.get(0)).thenReturn(1);
        when(mockList.get(1)).thenReturn(2);
        when(mockList.get(2)).thenReturn(3);

        // Inject the stub into the class we want to test
        AddingMachine machine = new AddingMachine(mockList);

        // Test the method we care about
        assertEquals(1 + 2 + 3, machine.getTotalUsingLoop());

        // Verify the protocol between AddingMachine and the stubbed list
        verify(mockList).size();
        verify(mockList, times(3)).get(anyInt());
    }

    @Test
    public void getTotalUsingLoop_BDD() {
        // Create a stubbed list
        List<Integer> mockList = mock();

        // Set the expectations on the stub
        given(mockList.size()).willReturn(3);
        given(mockList.get(anyInt())).willReturn(1, 2, 3);

        // Inject the stub into the class we want to test
        AddingMachine machine = new AddingMachine(mockList);

        // Test the method we care about
        assertEquals(1 + 2 + 3, machine.getTotalUsingLoop());

        // Verify the protocol between AddingMachine and the stubbed list
        then(mockList).should().size();
        then(mockList).should(times(3))
                .get(intThat(n -> n >= 0 && n < 3));
        // then(mockList).should(times(0)).remove(anyInt());
        then(mockList).shouldHaveNoMoreInteractions();
    }

    @Test // @SuppressWarnings("unchecked")
    public void getTotalUsingIterable() {
        List<Integer> mockList = mock();

        when(mockList.iterator()).thenReturn(
                Arrays.asList(1, 2, 3).iterator());

        AddingMachine machine = new AddingMachine(mockList);
        assertEquals(1 + 2 + 3, machine.getTotalUsingIterable());

        verify(mockList).iterator();
    }

    @Test
    public void getTotalUsingStream() {
        List<Integer> mockList = mock();
        when(mockList.stream()).thenReturn(Stream.of(1, 2, 3));
        // when(mockList.size()).thenReturn(3); // In JUnit 5, which is strict, this is not allowed

        AddingMachine machine = new AddingMachine(mockList);

        assertEquals(1 + 2 + 3, machine.getTotalUsingStream());

        verify(mockList).stream();
    }

    @Test
    public void getTotalUsingMockedIntegerList() {
        // Write our own mock implementation of List<Integer>
        // Only the size() and get(0), get(1), and get(2) methods are stubbed
        List<Integer> mockList = new MockListOfInteger();

        // Inject the stub into the class we want to test
        AddingMachine machine = new AddingMachine(mockList);

        // Test the method we care about
        assertEquals(1 + 2 + 3, machine.getTotalUsingLoop());
    }

    @Test  // Not using Mockito at all
    public void getTotalWithRealList() {
        List<Integer> realList = List.of(1, 2, 3);

        AddingMachine machine = new AddingMachine(realList);

        assertEquals(1 + 2 + 3, machine.getTotalUsingLoop());
        assertEquals(1 + 2 + 3, machine.getTotalUsingIterable());
        assertEquals(1 + 2 + 3, machine.getTotalUsingStream());

        // No built-in way to verify the method calls on real list
    }

    @Test
    public void spyOnList() {
        // Spy on a real list
        List<Integer> spyList = spy(Arrays.asList(1, 2, 3));

        AddingMachine machine = new AddingMachine(spyList);

        assertEquals(1 + 2 + 3, machine.getTotalUsingLoop());
        assertEquals(1 + 2 + 3, machine.getTotalUsingIterable());
        assertEquals(1 + 2 + 3, machine.getTotalUsingStream());

        // Can verify a spy
        verify(spyList).size();
        verify(spyList, times(3)).get(anyInt());
        verify(spyList).iterator();
        verify(spyList).stream();
    }

    @Test
    public void partialMockOfList() {
        // Spy on a real list
        List<Integer> spyList = spy(List.of());

        // Stub the size() method
        when(spyList.size()).thenReturn(3);

        // assertFalse(spyList.isEmpty()); // Uh oh. Is it empty or not?
    }
}