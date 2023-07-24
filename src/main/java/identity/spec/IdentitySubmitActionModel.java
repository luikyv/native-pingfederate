package identity.spec;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentitySubmitActionModel {
    @Schema(description = "The user's identifier", required = true)
    private String username;
}
