package password.spec;

import com.pingidentity.sdk.api.authn.common.CommonActionSpec;
import com.pingidentity.sdk.api.authn.common.CommonErrorSpec;
import com.pingidentity.sdk.api.authn.spec.AuthnActionSpec;
import com.pingidentity.sdk.api.authn.spec.AuthnErrorDetailSpec;
import com.pingidentity.sdk.api.authn.spec.AuthnStateSpec;
import identity.spec.IdentitySpec;

public class PasswordSpec {
    public final static AuthnErrorDetailSpec INVALID_PASSWORD = new AuthnErrorDetailSpec.Builder()
            .code("INVALID_PASSWORD")
            .message("The password is not valid")
            .parentCode(CommonErrorSpec.VALIDATION_ERROR.getCode())
            .build();

    public final static AuthnActionSpec<PasswordSubmitActionModel> SUBMIT_PASSWORD_ACTION = new AuthnActionSpec.Builder<PasswordSubmitActionModel>()
            .id("submitPassword")
            .description("Submit the user's password")
            .modelClass(PasswordSubmitActionModel.class)
            .error(CommonErrorSpec.VALIDATION_ERROR)
            .errorDetail(PasswordSpec.INVALID_PASSWORD)
            .build();

    public final static AuthnStateSpec<PasswordStateModel> PASSWORD_STATE = new AuthnStateSpec.Builder<PasswordStateModel>()
            .status("PASSWORD")
            .description("The user's password is required")
            .modelClass(PasswordStateModel.class)
            .action(PasswordSpec.SUBMIT_PASSWORD_ACTION)
            .action(CommonActionSpec.CANCEL)
            .build();
}
