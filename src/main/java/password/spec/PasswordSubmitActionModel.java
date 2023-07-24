package password.spec;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordSubmitActionModel {
    @Schema(description = "The user's password")
    private String password;
}
