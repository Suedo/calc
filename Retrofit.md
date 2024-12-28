## **Polymorphic Serialization with Retrofit and Jackson**

*Note: I later replaced Retrofit with RestClient for easier Observability out of the box, but even with restClient,
tried to use a similar concept*

This guide explains how to set up **Retrofit** with **Jackson** for handling polymorphic serialization and
deserialization of a sealed hierarchy in a **Java 21 Spring Boot** application. This setup is ideal for scenarios where
a `sealed interface` acts as a domain model, and its subtypes need to be serialized/deserialized seamlessly.

---

### **Overview: Key Features of Jackson for Polymorphism**

- Adds a `type` discriminator field to JSON during serialization.
- Automatically identifies subtypes during deserialization based on the `type` field.
- Supports Java 17+ features, including records.

---

### **1. Define the Sealed Hierarchy**

Use `@JsonTypeInfo` and `@JsonSubTypes` to guide Jackson for polymorphic (de)serialization.

```java
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// Sealed interface
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Dog.class, name = "dog"),
        @JsonSubTypes.Type(value = Cat.class, name = "cat")
})
public sealed interface Animal permits Dog, Cat {
}

// Record types
public record Dog(String name, String breed) implements Animal {
}

public record Cat(String name, int livesLeft) implements Animal {
}
```

#### **Example JSON Payloads**

- For `Dog`:
  ```json
  {
    "type": "dog",  // automatically added by Jackson
    "name": "Rex",
    "breed": "Labrador"
  }
  ```

- For `Cat`:
  ```json
  {
    "type": "cat",
    "name": "Whiskers",
    "livesLeft": 9
  }
  ```

---

### **2. Configure Jackson in Spring Boot**

Ensure the application uses a properly configured `ObjectMapper`.

1. **Expose an `ObjectMapper` Bean**:
   ```java
   import com.fasterxml.jackson.databind.ObjectMapper;

   @Configuration
   public class AppConfig {
       @Bean
       public ObjectMapper objectMapper() {
           return new ObjectMapper()
               .findAndRegisterModules(); // Automatically supports records
       }
   }
   ```

---

### **3. Configure Retrofit with Jackson**

Set up Retrofit to use Jackson’s `ObjectMapper` for consistent (de)serialization.

1. **Add Retrofit Dependency**:
   Add the Retrofit and Jackson Converter dependencies to `pom.xml`:

   ```xml
   <dependency>
       <groupId>com.squareup.retrofit2</groupId>
       <artifactId>retrofit</artifactId>
       <version>${version}</version>
   </dependency>
   <dependency>
       <groupId>com.squareup.retrofit2</groupId>
       <artifactId>converter-jackson</artifactId>
       <version>${version}</version>
   </dependency>
   ```

2. **Configure Retrofit**:
   ```java
   import retrofit2.Retrofit;
   import retrofit2.converter.jackson.JacksonConverterFactory;

   @Configuration
   public class RetrofitConfig {
       @Bean
       public Retrofit retrofit(ObjectMapper objectMapper) {
           return new Retrofit.Builder()
               .baseUrl("https://api.example.com")
               .addConverterFactory(JacksonConverterFactory.create(objectMapper))
               .build();
       }
   }
   ```

---

## **4. Define Retrofit API**

Create an API interface to handle requests and responses using the `Animal` sealed hierarchy.

```java
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

public interface AnimalService {
    @POST("/animals")
    Call<Void> saveAnimal(@Body Animal animal);

    @GET("/animals")
    Call<List<Animal>> getAnimals();
}
```

---

## **5. Usage Examples**

### **Serialize and Send an Animal**

Example Controller to serialize and send a `Dog`:

```java

@RestController
public class AnimalController {
    private final AnimalService animalService;

    public AnimalController(Retrofit retrofit) {
        this.animalService = retrofit.create(AnimalService.class);
    }

    @PostMapping("/animals")
    public void addAnimal() throws IOException {
        Animal dog = new Dog("Rex", "Labrador");

        Response<Void> response = animalService.saveAnimal(dog).execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to save animal: " + response.errorBody().string());
        }
    }
}
```

### **Deserialize and Fetch Animals**

Example to deserialize a list of animals:

```java

@GetMapping("/animals")
public List<Animal> fetchAnimals() throws IOException {
    Response<List<Animal>> response = animalService.getAnimals().execute();
    if (response.isSuccessful()) {
        return response.body();
    } else {
        throw new RuntimeException("Failed to fetch animals: " + response.errorBody().string());
    }
}
```

---

## **6. Verify Polymorphic Serialization**

To test serialization logic manually:

```java
public static void main(String[] args) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    Animal dog = new Dog("Rex", "Labrador");
    String json = objectMapper.writeValueAsString(dog);

    System.out.println(json); // {"type":"dog","name":"Rex","breed":"Labrador"}
}
```

---

## **Key Notes**

1. **Type Field**: The `type` field is added during serialization and determines the subtype during deserialization.
2. **Error Handling**: Ensure to handle errors for unknown or missing `type` fields during deserialization.
3. **Jackson Integration**: Using Jackson ensures consistency between Spring Boot and Retrofit serialization logic.

---

This setup allows the seamless use of `sealed` hierarchies as domain models with Retrofit and Jackson in a Spring Boot
application.