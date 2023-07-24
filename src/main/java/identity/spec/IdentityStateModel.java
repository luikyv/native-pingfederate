package identity.spec;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityStateModel {
    @Schema(description = "The type of username to be submitted")
    private UsernameType usernameType;
}
