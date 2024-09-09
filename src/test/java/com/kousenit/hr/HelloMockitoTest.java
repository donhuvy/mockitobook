package com.kousenit.hr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelloMockitoTest {
    @Mock
    private PersonRepository repository;

    @Mock
    private TranslationService translationService;

    @InjectMocks
    private HelloMockito helloMockito;

    @Test
    @DisplayName("Greet Admiral Hopper")
    void greetForPersonThatExists() {
        // Set the expectations on the dependencies
        when(repository.findById(anyInt()))
                .thenReturn(Optional.of(new Person(1, "Grace", "Hopper", LocalDate.now())));
        when(translationService.translate(
                "Hello, Grace, from Mockito!", "en", "en"))
                .thenReturn("Hello, Grace, from Mockito!");

        // Test the greet method
        String greeting = helloMockito.greet(1, "en", "en");
        assertEquals("Hello, Grace, from Mockito!", greeting);

        // Verify that the dependencies were called as expected
        InOrder inOrder = inOrder(repository, translationService);
        inOrder.verify(repository)
                .findById(anyInt());
        inOrder.verify(translationService)
                .translate(anyString(), eq("en"), eq("en"));
    }

    @Test
    @DisplayName("Greet a person not in the database")
    void greetForPersonThatDoesNotExist() {
        when(repository.findById(anyInt()))
                .thenReturn(Optional.empty());
        when(translationService.translate(
                "Hello, World, from Mockito!", "en", "en"))
                .thenReturn("Hello, World, from Mockito!");

        String greeting = helloMockito.greet(100, "en", "en");
        assertEquals("Hello, World, from Mockito!", greeting);

        InOrder inOrder = inOrder(repository, translationService);
        inOrder.verify(repository)
                .findById(anyInt());
        inOrder.verify(translationService)
                .translate(anyString(), eq("en"), eq("en"));
    }

    @Test
    void greetWithDefaultTranslator() {
        PersonRepository mockRepo = mock(PersonRepository.class);
        when(mockRepo.findById(anyInt()))
                .thenReturn(Optional.of(new Person(1, "Grace", "Hopper", LocalDate.now())));
        HelloMockito helloMockito = new HelloMockito(mockRepo);
        String greeting = helloMockito.greet(1, "en", "en");
        assertThat(greeting).isEqualTo("Hello, Grace, from Mockito!");
    }

    @Test
    void greetWithMockedConstructor() {
        // Mock for repo (needed for HelloMockito constructor)
        PersonRepository mockRepo = mock(PersonRepository.class);
        when(mockRepo.findById(anyInt()))
                .thenReturn(Optional.of(new Person(1, "Grace", "Hopper", LocalDate.now())));

        // Mock for translator (instantiated inside HelloMockito constructor)
        try (MockedConstruction<DefaultTranslationService> ignored =
                     mockConstruction(DefaultTranslationService.class,
                             (mock, context) -> when(mock.translate(anyString(), anyString(), anyString()))
                                     .thenAnswer(invocation -> invocation.getArgument(0) + " (translated)"))) {

            // Instantiate HelloMockito with mocked repo and locally instantiated translator
            HelloMockito hello = new HelloMockito(mockRepo);
            String greeting = hello.greet(1, "en", "en");
            assertThat(greeting).isEqualTo("Hello, Grace, from Mockito! (translated)");

            // Any instantiation of DefaultTranslationService will return the mocked instance
            DefaultTranslationService translator = new DefaultTranslationService();
            String translate = translator.translate("What up?", "en", "en");
            assertThat(translate).isEqualTo("What up? (translated)");
        }
    }

    @Test
    void greetWithMockedConstructorWithAnswer() {
        // Mock for repo (needed for HelloMockito constructor)
        PersonRepository mockRepo = mock(PersonRepository.class);
        when(mockRepo.findById(anyInt()))
                .thenReturn(Optional.of(new Person(1, "Grace", "Hopper", LocalDate.now())));

        // Mock for translator (instantiated inside HelloMockito constructor)
        try (MockedConstruction<DefaultTranslationService> ignored =
                     mockConstructionWithAnswer(DefaultTranslationService.class,
                             invocation -> invocation.getArgument(0) + " (translated)",
                             invocation -> invocation.getArgument(0) + " (translated again)")) {

            // Instantiate HelloMockito with mocked repo and locally instantiated translator
            HelloMockito hello = new HelloMockito(mockRepo);
            String greeting = hello.greet(1, "en", "en");
            assertThat(greeting).isEqualTo("Hello, Grace, from Mockito! (translated)");
        }
    }

    @Test
    void testGetterAndSetter() {
        assertThat(helloMockito.getGreeting()).isNotNull();
        assertThat(helloMockito.getGreeting()).isEqualTo("Hello, %s, from Mockito!");

        helloMockito.setGreeting("Hi there, %s, from Mockito!");
        assertThat(helloMockito.getGreeting()).isEqualTo("Hi there, %s, from Mockito!");
    }

    @Test
    @DisplayName("Integration test without mocks")
    void helloMockitoWithExplicitStubs() {
        PersonRepository personRepo = new InMemoryPersonRepository();

        helloMockito = new HelloMockito(
                personRepo,
                new DefaultTranslationService()
        );

        // Save a person
        Person person = new Person(1, "Grace", "Hopper", LocalDate.now());
        personRepo.save(person);

        // Greet a user that exists
        String greeting = helloMockito.greet(1, "en", "en");
        assertThat(greeting).isEqualTo("Hello, Grace, from Mockito!");

        // Greet a user that does not exist
        greeting = helloMockito.greet(100, "en", "en");
        assertThat(greeting).isEqualTo("Hello, World, from Mockito!");
    }

    @Test
    @DisplayName("Greet Admiral Hopper")
    void greetAPersonUsingAnswers() {
        // Set the expectations on the dependencies
        when(repository.findById(anyInt()))
                .thenReturn(Optional.of(new Person(1, "Grace", "Hopper", LocalDate.now())));
        when(translationService.translate(
                anyString(), eq("en"), eq("en")))
                .thenAnswer(returnsFirstArg());

        // Test the greet method
        String greeting = helloMockito.greet(1, "en", "en");
        assertEquals("Hello, Grace, from Mockito!", greeting);

        // Verify that the dependencies were called as expected
        verify(repository)
                .findById(anyInt());
        verify(translationService)
                // gives an error: if one arg is an argument matcher, they all have to be
                // .translate(anyString(), "en", "en");
                .translate(anyString(), eq("en"), eq("en"));
    }

    @Test
    void greetPersonWithSpecifiedLanguages() {
        Person hopper = new Person(1, "Grace", "Hopper",
                LocalDate.of(1906, 12, 9));

        TranslationService mockTranslator = mock(TranslationService.class);
        when(mockTranslator.translate(anyString(), anyString(), anyString()))
                .thenReturn(String.format("Hello, %s, from Mockito", hopper.getFirst())
                        + " (translated)");

        HelloMockito helloMockito = new HelloMockito(mockTranslator);
        String greeting = helloMockito.greet(hopper, "en", "en");
        assertThat(greeting).isEqualTo("Hello, Grace, from Mockito (translated)");
    }
}