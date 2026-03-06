# Prompt Templates in LangChain (Java)

## 🎯 Overview
Prompt Templates in LangChain provide a structured approach to creating, managing, and optimizing prompts for language models. They enable dynamic prompt generation, variable substitution, and consistent prompt formatting across different use cases.

## 🧠 Core Template Concepts

### What are Prompt Templates?
Prompt templates are structured patterns that:
- **Standardize Prompts**: Create consistent prompt formats
- **Enable Variables**: Support dynamic content insertion  
- **Improve Reusability**: Share templates across different contexts
- **Facilitate Testing**: A/B test different prompt variations
- **Support Localization**: Multi-language prompt support

### Template Types
1. **Simple Templates**: Basic variable substitution
2. **Chat Templates**: Conversation-aware prompts
3. **Few-Shot Templates**: Example-based prompts
4. **Conditional Templates**: Logic-based prompt selection
5. **Composite Templates**: Multi-part complex prompts

## 🏗️ Template Architecture Patterns

### 1. **Variable Substitution**
```
Template: "Translate '{text}' from {source_lang} to {target_lang}"
Variables: text="Hello", source_lang="English", target_lang="Spanish"
Result: "Translate 'Hello' from English to Spanish"
```

### 2. **Few-Shot Pattern**
```
Template: Examples + Instruction + New Input
Examples: Q: ... A: ...
Instruction: Based on the examples above...
Input: New question
```

### 3. **Conditional Logic**
```
IF condition THEN template_A ELSE template_B
```

### 4. **Hierarchical Templates**
```
Main Template
├── Header Template
├── Context Template  
└── Footer Template
```

## 💻 Java Template Implementation

### Base Template Framework
```java
package com.example.agent.prompts;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;

/**
 * Base interface for all prompt templates
 */
public interface PromptTemplate {
    
    /**
     * Format the template with provided variables
     */
    String format(Map<String, Object> variables) throws TemplateException;
    
    /**
     * Get required variable names
     */
    Set<String> getRequiredVariables();
    
    /**
     * Get optional variable names with defaults
     */
    Map<String, Object> getOptionalVariables();
    
    /**
     * Get template description
     */
    String getDescription();
    
    /**
     * Validate that all required variables are provided
     */
    default boolean validate(Map<String, Object> variables) {
        return variables.keySet().containsAll(getRequiredVariables());
    }
}

/**
 * Template formatting exception
 */
public class TemplateException extends Exception {
    public TemplateException(String message) {
        super(message);
    }
    
    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Simple string-based prompt template
 */
public class StringPromptTemplate implements PromptTemplate {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    private final String template;
    private final String description;
    private final Set<String> requiredVariables;
    private final Map<String, Object> optionalVariables;
    
    public StringPromptTemplate(String template, String description) {
        this(template, description, Map.of());
    }
    
    public StringPromptTemplate(String template, String description, 
                               Map<String, Object> optionalVariables) {
        this.template = template;
        this.description = description;
        this.optionalVariables = new HashMap<>(optionalVariables);
        this.requiredVariables = extractRequiredVariables(template);
    }
    
    @Override
    public String format(Map<String, Object> variables) throws TemplateException {
        if (!validate(variables)) {
            Set<String> missing = new HashSet<>(requiredVariables);
            missing.removeAll(variables.keySet());
            throw new TemplateException("Missing required variables: " + missing);
        }
        
        // Merge provided variables with optional defaults
        Map<String, Object> allVariables = new HashMap<>(optionalVariables);
        allVariables.putAll(variables);
        
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = allVariables.get(variableName);
            
            if (value != null) {
                String replacement = value.toString();
                result = result.replace("{" + variableName + "}", replacement);
            }
        }
        
        return result;
    }
    
    @Override
    public Set<String> getRequiredVariables() {
        return new HashSet<>(requiredVariables);
    }
    
    @Override
    public Map<String, Object> getOptionalVariables() {
        return new HashMap<>(optionalVariables);
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    private Set<String> extractRequiredVariables(String template) {
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (!optionalVariables.containsKey(variableName)) {
                variables.add(variableName);
            }
        }
        
        return variables;
    }
    
    public String getTemplate() {
        return template;
    }
}

/**
 * Chat-specific prompt template with role management
 */
public class ChatPromptTemplate implements PromptTemplate {
    
    private final List<ChatMessageTemplate> messageTemplates;
    private final String description;
    private final Set<String> requiredVariables;
    private final Map<String, Object> optionalVariables;
    
    public ChatPromptTemplate(List<ChatMessageTemplate> messageTemplates, String description) {
        this(messageTemplates, description, Map.of());
    }
    
    public ChatPromptTemplate(List<ChatMessageTemplate> messageTemplates, String description,
                             Map<String, Object> optionalVariables) {
        this.messageTemplates = new ArrayList<>(messageTemplates);
        this.description = description;
        this.optionalVariables = new HashMap<>(optionalVariables);
        this.requiredVariables = extractAllRequiredVariables();
    }
    
    @Override
    public String format(Map<String, Object> variables) throws TemplateException {
        if (!validate(variables)) {
            Set<String> missing = new HashSet<>(requiredVariables);
            missing.removeAll(variables.keySet());
            throw new TemplateException("Missing required variables: " + missing);
        }
        
        Map<String, Object> allVariables = new HashMap<>(optionalVariables);
        allVariables.putAll(variables);
        
        StringBuilder result = new StringBuilder();
        
        for (ChatMessageTemplate messageTemplate : messageTemplates) {
            String formattedMessage = messageTemplate.format(allVariables);
            result.append(messageTemplate.getRole()).append(": ")
                  .append(formattedMessage).append("\n");
        }
        
        return result.toString().trim();
    }
    
    @Override
    public Set<String> getRequiredVariables() {
        return new HashSet<>(requiredVariables);
    }
    
    @Override
    public Map<String, Object> getOptionalVariables() {
        return new HashMap<>(optionalVariables);
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    private Set<String> extractAllRequiredVariables() {
        Set<String> variables = new HashSet<>();
        for (ChatMessageTemplate messageTemplate : messageTemplates) {
            variables.addAll(messageTemplate.getRequiredVariables());
        }
        variables.removeAll(optionalVariables.keySet());
        return variables;
    }
    
    public List<ChatMessageTemplate> getMessageTemplates() {
        return new ArrayList<>(messageTemplates);
    }
}

/**
 * Individual chat message template
 */
public class ChatMessageTemplate {
    
    private final String role; // "system", "user", "assistant"
    private final StringPromptTemplate template;
    
    public ChatMessageTemplate(String role, String template, String description) {
        this.role = role;
        this.template = new StringPromptTemplate(template, description);
    }
    
    public String format(Map<String, Object> variables) throws TemplateException {
        return template.format(variables);
    }
    
    public Set<String> getRequiredVariables() {
        return template.getRequiredVariables();
    }
    
    public String getRole() {
        return role;
    }
    
    public String getTemplate() {
        return template.getTemplate();
    }
}

/**
 * Few-shot prompt template with examples
 */
public class FewShotPromptTemplate implements PromptTemplate {
    
    private final List<Example> examples;
    private final StringPromptTemplate exampleTemplate;
    private final StringPromptTemplate prefixTemplate;
    private final StringPromptTemplate suffixTemplate;
    private final String description;
    
    public FewShotPromptTemplate(List<Example> examples, 
                                StringPromptTemplate exampleTemplate,
                                StringPromptTemplate prefixTemplate,
                                StringPromptTemplate suffixTemplate,
                                String description) {
        this.examples = new ArrayList<>(examples);
        this.exampleTemplate = exampleTemplate;
        this.prefixTemplate = prefixTemplate;
        this.suffixTemplate = suffixTemplate;
        this.description = description;
    }
    
    @Override
    public String format(Map<String, Object> variables) throws TemplateException {
        StringBuilder result = new StringBuilder();
        
        // Add prefix if available
        if (prefixTemplate != null) {
            result.append(prefixTemplate.format(variables)).append("\n\n");
        }
        
        // Add examples
        for (Example example : examples) {
            Map<String, Object> exampleVars = new HashMap<>(variables);
            exampleVars.putAll(example.getVariables());
            
            result.append(exampleTemplate.format(exampleVars)).append("\n\n");
        }
        
        // Add suffix if available
        if (suffixTemplate != null) {
            result.append(suffixTemplate.format(variables));
        }
        
        return result.toString().trim();
    }
    
    @Override
    public Set<String> getRequiredVariables() {
        Set<String> variables = new HashSet<>();
        
        if (prefixTemplate != null) {
            variables.addAll(prefixTemplate.getRequiredVariables());
        }
        
        if (suffixTemplate != null) {
            variables.addAll(suffixTemplate.getRequiredVariables());
        }
        
        // Add example template variables that aren't provided by examples
        Set<String> exampleVars = exampleTemplate.getRequiredVariables();
        for (String var : exampleVars) {
            if (examples.stream().noneMatch(ex -> ex.getVariables().containsKey(var))) {
                variables.add(var);
            }
        }
        
        return variables;
    }
    
    @Override
    public Map<String, Object> getOptionalVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        if (prefixTemplate != null) {
            variables.putAll(prefixTemplate.getOptionalVariables());
        }
        
        if (suffixTemplate != null) {
            variables.putAll(suffixTemplate.getOptionalVariables());
        }
        
        variables.putAll(exampleTemplate.getOptionalVariables());
        
        return variables;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public List<Example> getExamples() {
        return new ArrayList<>(examples);
    }
}

/**
 * Example for few-shot templates
 */
public class Example {
    private final Map<String, Object> variables;
    
    public Example(Map<String, Object> variables) {
        this.variables = new HashMap<>(variables);
    }
    
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }
    
    public static Example of(String key1, Object value1) {
        return new Example(Map.of(key1, value1));
    }
    
    public static Example of(String key1, Object value1, String key2, Object value2) {
        return new Example(Map.of(key1, value1, key2, value2));
    }
    
    public static Example of(String key1, Object value1, String key2, Object value2, 
                           String key3, Object value3) {
        return new Example(Map.of(key1, value1, key2, value2, key3, value3));
    }
}
```

### Conditional and Dynamic Templates
```java
/**
 * Conditional prompt template that selects based on conditions
 */
public class ConditionalPromptTemplate implements PromptTemplate {
    
    private final List<ConditionTemplate> conditionTemplates;
    private final PromptTemplate defaultTemplate;
    private final String description;
    
    public ConditionalPromptTemplate(List<ConditionTemplate> conditionTemplates,
                                   PromptTemplate defaultTemplate,
                                   String description) {
        this.conditionTemplates = new ArrayList<>(conditionTemplates);
        this.defaultTemplate = defaultTemplate;
        this.description = description;
    }
    
    @Override
    public String format(Map<String, Object> variables) throws TemplateException {
        // Evaluate conditions in order
        for (ConditionTemplate conditionTemplate : conditionTemplates) {
            if (conditionTemplate.evaluateCondition(variables)) {
                return conditionTemplate.getTemplate().format(variables);
            }
        }
        
        // Use default template if no conditions match
        if (defaultTemplate != null) {
            return defaultTemplate.format(variables);
        }
        
        throw new TemplateException("No matching condition and no default template");
    }
    
    @Override
    public Set<String> getRequiredVariables() {
        Set<String> variables = new HashSet<>();
        
        for (ConditionTemplate conditionTemplate : conditionTemplates) {
            variables.addAll(conditionTemplate.getRequiredVariables());
        }
        
        if (defaultTemplate != null) {
            variables.addAll(defaultTemplate.getRequiredVariables());
        }
        
        return variables;
    }
    
    @Override
    public Map<String, Object> getOptionalVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        for (ConditionTemplate conditionTemplate : conditionTemplates) {
            variables.putAll(conditionTemplate.getTemplate().getOptionalVariables());
        }
        
        if (defaultTemplate != null) {
            variables.putAll(defaultTemplate.getOptionalVariables());
        }
        
        return variables;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
}

/**
 * Condition template with evaluation logic
 */
public class ConditionTemplate {
    private final Function<Map<String, Object>, Boolean> condition;
    private final PromptTemplate template;
    private final String conditionDescription;
    
    public ConditionTemplate(Function<Map<String, Object>, Boolean> condition,
                           PromptTemplate template,
                           String conditionDescription) {
        this.condition = condition;
        this.template = template;
        this.conditionDescription = conditionDescription;
    }
    
    public boolean evaluateCondition(Map<String, Object> variables) {
        try {
            return condition.apply(variables);
        } catch (Exception e) {
            return false;
        }
    }
    
    public PromptTemplate getTemplate() {
        return template;
    }
    
    public Set<String> getRequiredVariables() {
        return template.getRequiredVariables();
    }
    
    public String getConditionDescription() {
        return conditionDescription;
    }
}

/**
 * Composite template that combines multiple templates
 */
public class CompositePromptTemplate implements PromptTemplate {
    
    private final List<TemplateSection> sections;
    private final String description;
    private final String separator;
    
    public CompositePromptTemplate(List<TemplateSection> sections, String description) {
        this(sections, description, "\n\n");
    }
    
    public CompositePromptTemplate(List<TemplateSection> sections, String description, 
                                  String separator) {
        this.sections = new ArrayList<>(sections);
        this.description = description;
        this.separator = separator;
    }
    
    @Override
    public String format(Map<String, Object> variables) throws TemplateException {
        List<String> formattedSections = new ArrayList<>();
        
        for (TemplateSection section : sections) {
            if (section.shouldInclude(variables)) {
                String formatted = section.getTemplate().format(variables);
                if (!formatted.trim().isEmpty()) {
                    formattedSections.add(formatted);
                }
            }
        }
        
        return String.join(separator, formattedSections);
    }
    
    @Override
    public Set<String> getRequiredVariables() {
        Set<String> variables = new HashSet<>();
        
        for (TemplateSection section : sections) {
            if (section.isRequired()) {
                variables.addAll(section.getTemplate().getRequiredVariables());
            }
        }
        
        return variables;
    }
    
    @Override
    public Map<String, Object> getOptionalVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        for (TemplateSection section : sections) {
            variables.putAll(section.getTemplate().getOptionalVariables());
        }
        
        return variables;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
}

/**
 * Section in a composite template
 */
public class TemplateSection {
    private final PromptTemplate template;
    private final Function<Map<String, Object>, Boolean> includeCondition;
    private final boolean required;
    private final String sectionName;
    
    public TemplateSection(String sectionName, PromptTemplate template) {
        this(sectionName, template, vars -> true, true);
    }
    
    public TemplateSection(String sectionName, PromptTemplate template, 
                          Function<Map<String, Object>, Boolean> includeCondition) {
        this(sectionName, template, includeCondition, false);
    }
    
    public TemplateSection(String sectionName, PromptTemplate template,
                          Function<Map<String, Object>, Boolean> includeCondition,
                          boolean required) {
        this.sectionName = sectionName;
        this.template = template;
        this.includeCondition = includeCondition;
        this.required = required;
    }
    
    public boolean shouldInclude(Map<String, Object> variables) {
        try {
            return includeCondition.apply(variables);
        } catch (Exception e) {
            return false;
        }
    }
    
    public PromptTemplate getTemplate() {
        return template;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public String getSectionName() {
        return sectionName;
    }
}
```

### Template Builder and Registry
```java
/**
 * Fluent builder for creating prompt templates
 */
public class PromptTemplateBuilder {
    
    public static StringPromptTemplateBuilder string(String template) {
        return new StringPromptTemplateBuilder(template);
    }
    
    public static ChatPromptTemplateBuilder chat() {
        return new ChatPromptTemplateBuilder();
    }
    
    public static FewShotPromptTemplateBuilder fewShot() {
        return new FewShotPromptTemplateBuilder();
    }
    
    public static ConditionalPromptTemplateBuilder conditional() {
        return new ConditionalPromptTemplateBuilder();
    }
    
    public static CompositePromptTemplateBuilder composite() {
        return new CompositePromptTemplateBuilder();
    }
}

/**
 * Builder for string templates
 */
class StringPromptTemplateBuilder {
    private final String template;
    private String description = "";
    private Map<String, Object> optionalVariables = new HashMap<>();
    
    public StringPromptTemplateBuilder(String template) {
        this.template = template;
    }
    
    public StringPromptTemplateBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public StringPromptTemplateBuilder optional(String key, Object value) {
        this.optionalVariables.put(key, value);
        return this;
    }
    
    public StringPromptTemplateBuilder optionals(Map<String, Object> optionals) {
        this.optionalVariables.putAll(optionals);
        return this;
    }
    
    public StringPromptTemplate build() {
        return new StringPromptTemplate(template, description, optionalVariables);
    }
}

/**
 * Builder for chat templates
 */
class ChatPromptTemplateBuilder {
    private List<ChatMessageTemplate> messages = new ArrayList<>();
    private String description = "";
    private Map<String, Object> optionalVariables = new HashMap<>();
    
    public ChatPromptTemplateBuilder system(String template) {
        messages.add(new ChatMessageTemplate("system", template, "System message"));
        return this;
    }
    
    public ChatPromptTemplateBuilder user(String template) {
        messages.add(new ChatMessageTemplate("user", template, "User message"));
        return this;
    }
    
    public ChatPromptTemplateBuilder assistant(String template) {
        messages.add(new ChatMessageTemplate("assistant", template, "Assistant message"));
        return this;
    }
    
    public ChatPromptTemplateBuilder message(String role, String template) {
        messages.add(new ChatMessageTemplate(role, template, role + " message"));
        return this;
    }
    
    public ChatPromptTemplateBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public ChatPromptTemplateBuilder optional(String key, Object value) {
        this.optionalVariables.put(key, value);
        return this;
    }
    
    public ChatPromptTemplate build() {
        return new ChatPromptTemplate(messages, description, optionalVariables);
    }
}

/**
 * Template registry for managing and organizing templates
 */
@Service
public class PromptTemplateRegistry {
    
    private final Map<String, PromptTemplate> templates;
    private final Map<String, String> categories;
    
    public PromptTemplateRegistry() {
        this.templates = new ConcurrentHashMap<>();
        this.categories = new ConcurrentHashMap<>();
        initializeBuiltInTemplates();
    }
    
    /**
     * Register a template with a unique name
     */
    public void register(String name, PromptTemplate template, String category) {
        templates.put(name, template);
        categories.put(name, category);
    }
    
    /**
     * Get template by name
     */
    public Optional<PromptTemplate> get(String name) {
        return Optional.ofNullable(templates.get(name));
    }
    
    /**
     * List templates by category
     */
    public List<String> getTemplatesByCategory(String category) {
        return categories.entrySet().stream()
            .filter(entry -> category.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .toList();
    }
    
    /**
     * Get all template names
     */
    public Set<String> getAllTemplateNames() {
        return new HashSet<>(templates.keySet());
    }
    
    /**
     * Format template with variables
     */
    public String format(String templateName, Map<String, Object> variables) throws TemplateException {
        PromptTemplate template = templates.get(templateName);
        if (template == null) {
            throw new TemplateException("Template not found: " + templateName);
        }
        
        return template.format(variables);
    }
    
    private void initializeBuiltInTemplates() {
        // Basic Q&A template
        register("basic_qa", 
            PromptTemplateBuilder.string("Question: {question}\nAnswer: ")
                .description("Basic question-answering template")
                .build(),
            "qa"
        );
        
        // Translation template
        register("translation",
            PromptTemplateBuilder.string("Translate the following text from {source_lang} to {target_lang}:\n\n{text}\n\nTranslation:")
                .description("Language translation template")
                .build(),
            "translation"
        );
        
        // Summarization template
        register("summarization",
            PromptTemplateBuilder.string("Summarize the following text in {max_sentences} sentences:\n\n{text}\n\nSummary:")
                .description("Text summarization template")
                .optional("max_sentences", "3")
                .build(),
            "summarization"
        );
        
        // Code explanation template
        register("code_explanation",
            PromptTemplateBuilder.string("Explain this {language} code:\n\n```{language}\n{code}\n```\n\nExplanation:")
                .description("Code explanation template")
                .build(),
            "code"
        );
        
        // Few-shot classification template
        List<Example> classificationExamples = List.of(
            Example.of("text", "I love this product!", "label", "positive"),
            Example.of("text", "This is terrible quality", "label", "negative"),
            Example.of("text", "It's okay, nothing special", "label", "neutral")
        );
        
        register("sentiment_classification",
            new FewShotPromptTemplate(
                classificationExamples,
                new StringPromptTemplate("Text: {text}\nSentiment: {label}", "Example format"),
                new StringPromptTemplate("Classify the sentiment of the following texts:", "Prefix"),
                new StringPromptTemplate("Text: {text}\nSentiment:", "Task"),
                "Few-shot sentiment classification"
            ),
            "classification"
        );
        
        // Conversational template
        register("conversation",
            PromptTemplateBuilder.chat()
                .system("You are a helpful assistant. Be concise and friendly.")
                .user("History: {history}")
                .user("Current question: {question}")
                .description("Conversational chat template")
                .optional("history", "")
                .build(),
            "conversation"
        );
    }
}
```

### Advanced Template Features
```java
/**
 * Template with validation and constraints
 */
public class ValidatedPromptTemplate implements PromptTemplate {
    
    private final PromptTemplate innerTemplate;
    private final Map<String, Validator> validators;
    
    public ValidatedPromptTemplate(PromptTemplate innerTemplate, 
                                 Map<String, Validator> validators) {
        this.innerTemplate = innerTemplate;
        this.validators = new HashMap<>(validators);
    }
    
    @Override
    public String format(Map<String, Object> variables) throws TemplateException {
        // Validate variables first
        for (Map.Entry<String, Validator> entry : validators.entrySet()) {
            String key = entry.getKey();
            Validator validator = entry.getValue();
            
            Object value = variables.get(key);
            if (!validator.isValid(value)) {
                throw new TemplateException(String.format(
                    "Variable '%s' validation failed: %s", key, validator.getErrorMessage()));
            }
        }
        
        return innerTemplate.format(variables);
    }
    
    @Override
    public Set<String> getRequiredVariables() {
        return innerTemplate.getRequiredVariables();
    }
    
    @Override
    public Map<String, Object> getOptionalVariables() {
        return innerTemplate.getOptionalVariables();
    }
    
    @Override
    public String getDescription() {
        return innerTemplate.getDescription() + " (with validation)";
    }
}

/**
 * Variable validator interface
 */
public interface Validator {
    boolean isValid(Object value);
    String getErrorMessage();
}

/**
 * Common validators
 */
public class Validators {
    
    public static Validator notNull() {
        return new Validator() {
            @Override
            public boolean isValid(Object value) {
                return value != null;
            }
            
            @Override
            public String getErrorMessage() {
                return "Value cannot be null";
            }
        };
    }
    
    public static Validator notEmpty() {
        return new Validator() {
            @Override
            public boolean isValid(Object value) {
                return value != null && !value.toString().trim().isEmpty();
            }
            
            @Override
            public String getErrorMessage() {
                return "Value cannot be empty";
            }
        };
    }
    
    public static Validator maxLength(int maxLength) {
        return new Validator() {
            @Override
            public boolean isValid(Object value) {
                return value == null || value.toString().length() <= maxLength;
            }
            
            @Override
            public String getErrorMessage() {
                return "Value must be at most " + maxLength + " characters";
            }
        };
    }
    
    public static Validator pattern(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return new Validator() {
            @Override
            public boolean isValid(Object value) {
                return value == null || pattern.matcher(value.toString()).matches();
            }
            
            @Override
            public String getErrorMessage() {
                return "Value must match pattern: " + regex;
            }
        };
    }
    
    public static Validator oneOf(Set<String> allowedValues) {
        return new Validator() {
            @Override
            public boolean isValid(Object value) {
                return value == null || allowedValues.contains(value.toString());
            }
            
            @Override
            public String getErrorMessage() {
                return "Value must be one of: " + allowedValues;
            }
        };
    }
}

/**
 * Internationalization support for templates
 */
@Service
public class InternationalizedTemplateRegistry {
    
    private final Map<String, Map<Locale, PromptTemplate>> templates;
    private final Locale defaultLocale;
    
    public InternationalizedTemplateRegistry() {
        this.templates = new ConcurrentHashMap<>();
        this.defaultLocale = Locale.ENGLISH;
    }
    
    public void register(String name, PromptTemplate template, Locale locale) {
        templates.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).put(locale, template);
    }
    
    public Optional<PromptTemplate> get(String name, Locale locale) {
        Map<Locale, PromptTemplate> localeTemplates = templates.get(name);
        if (localeTemplates == null) {
            return Optional.empty();
        }
        
        // Try exact locale match first
        PromptTemplate template = localeTemplates.get(locale);
        if (template != null) {
            return Optional.of(template);
        }
        
        // Try language match (ignoring country)
        Locale languageLocale = new Locale(locale.getLanguage());
        template = localeTemplates.get(languageLocale);
        if (template != null) {
            return Optional.of(template);
        }
        
        // Fallback to default locale
        return Optional.ofNullable(localeTemplates.get(defaultLocale));
    }
    
    public String format(String name, Map<String, Object> variables, Locale locale) 
            throws TemplateException {
        Optional<PromptTemplate> template = get(name, locale);
        if (template.isEmpty()) {
            throw new TemplateException("Template not found: " + name + " for locale: " + locale);
        }
        
        return template.get().format(variables);
    }
}
```

## 🚀 Best Practices

1. **Template Design**
   - Keep templates focused and reusable
   - Use clear variable names
   - Provide good descriptions
   - Include validation where appropriate

2. **Variable Management**
   - Define clear required vs optional variables
   - Provide sensible defaults
   - Use consistent naming conventions
   - Validate input values

3. **Organization**
   - Use categories to organize templates
   - Create template libraries for different domains
   - Version templates for consistency
   - Document template usage patterns

4. **Performance**
   - Cache compiled templates
   - Validate templates at registration time
   - Use efficient variable substitution
   - Monitor template usage metrics

5. **Localization**
   - Support multiple languages
   - Use locale-specific formatting
   - Handle cultural differences
   - Provide fallback mechanisms

## 🔗 Integration with Other Components

Prompt Templates integrate with:
- **Chains**: Templates provide prompts for chain operations
- **Agents**: Agents use templates for reasoning and communication
- **Memory**: Templates can include conversation history
- **Tools**: Templates can format tool descriptions and results

---

*Next: [Vector Stores](../vectorstores/) - Learn about semantic search and retrieval-augmented generation.*
