package password.spec;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordStateModel {
    @Schema(description = "The user to be authenticated")
    private String username;
}
