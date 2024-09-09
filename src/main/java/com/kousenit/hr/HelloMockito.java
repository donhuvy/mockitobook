package com.kousenit.hr;

import java.util.Optional;

public class HelloMockito {

    private final PersonRepository personRepository;
    private final TranslationService translationService;
    private String greeting = "Hello, %s, from Mockito!";

    public HelloMockito(PersonRepository personRepository, TranslationService translationService) {
        this.personRepository = personRepository;
        this.translationService = translationService;
    }

    public HelloMockito(PersonRepository personRepository) {
        this(personRepository, new DefaultTranslationService());
    }


    public HelloMockito(TranslationService service) {
        this(new InMemoryPersonRepository(), service);
    }

    public String greet(int id, String sourceLanguage, String targetLanguage) {
        Optional<Person> person = personRepository.findById(id);
        String name = person.map(Person::getFirst).orElse("World");
        return translationService.translate(
                String.format(greeting, name), sourceLanguage, targetLanguage);
    }

    @SuppressWarnings("unused")
    public String greet(int id) {
        Optional<Person> person = personRepository.findById(id);
        String name = person.map(Person::getFirst).orElse("World");
        return translationService.translate(String.format(greeting, name));
    }

    public String greet(Person person, String sourceLanguage, String targetLanguage) {
        return translationService.translate(
                String.format(greeting, person.getFirst()), sourceLanguage, targetLanguage);
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
