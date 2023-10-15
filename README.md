# ProximateConfigurate

Represent your configuration through interfaces

## Introduction
ProximateConfigurate is a simple extension for [Configurate](https://github.com/SpongePowered/Configurate) that empowers you to represent your configuration data using Java interfaces. This extension offers a cleaner and more intuitive way to define and work with configuration data, resulting in more concise and readable code. With ProximateConfigurate, you can define configuration interfaces, add comments, default values, and perform actions on fields using transient methods.

### Objectives
ProximateConfigurate aims to:

1. **Simplify Configuration**: Use interfaces instead of classes for cleaner and more concise configuration definitions.

2. **Intuitive Representation**: Provide an intuitive way to define configuration data with comments and default values.

3. **Feature Parity**: Offer the same features available for classes when working with configuration.

### Getting Started
To get started with ProximateConfigurate, follow these steps:

1. **Add ProximateConfigurate to your project**: Include the ProximateConfigurate library in your project's dependencies.

2. **Define Configuration Interfaces**: Create Java interfaces that represent your configuration data. Annotate fields with comments and specify default values using the `@Comment` and `default` keywords.

```java
@ConfigInterface
public interface Configuration {
    @Comment("Simple comment on the first node")
    String first();

    // Define default values through the 'default' keyword
    default int someNumber() {
        return 4;
    }

    // Perform actions on fields with transient methods
    @Transient
    default String joinFields() {
        return first() + " " + someNumber();
    }
}
```

3. **Usage**: `TODO`

4. **Result**: 
Once you've defined your configuration interface, you can easily access and manipulate your configuration data. For example, with the above interface, the following YAML representation is generated:

```yaml
# Simple comment on the first node
first: Hello
```

You can then access the `joinFields` method to produce output: `Hello 4`.

ProximateConfigurate simplifies your configuration management by allowing you to work with clean and compact interfaces, making your code more maintainable and intuitive.

### Maven
`TODO`

### Gradle
`TODO`

## Contributing
We welcome contributions to ProximateConfigurate! If you have ideas for improvements or encounter issues, please feel free to open an issue or submit a pull request on the [repository](https://github.com/bivashy/ProximateConfigurate).

---

ProximateConfigurate simplifies configuration management by using interfaces for clean and concise definitions, offering feature parity with class-based configurations, and providing an intuitive way to represent and work with your configuration data.