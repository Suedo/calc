package example.demo.shared.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumberToken.class, name = "number"),
        @JsonSubTypes.Type(value = OperatorToken.class, name = "operator")
})
public sealed interface Token permits NumberToken, OperatorToken {
}

