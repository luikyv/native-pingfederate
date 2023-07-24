package identity.spec;

import com.pingidentity.sdk.api.authn.common.CommonActionSpec;
import com.pingidentity.sdk.api.authn.common.CommonErrorSpec;
import com.pingidentity.sdk.api.authn.spec.AuthnActionSpec;
import com.pingidentity.sdk.api.authn.spec.AuthnErrorDetailSpec;
import com.pingidentity.sdk.api.authn.spec.AuthnStateSpec;

public class IdentitySpec {
    public final static AuthnErrorDetailSpec INVALID_USERNAME = new AuthnErrorDetailSpec.Builder()
            .code("INVALID_USERNAME")
            .message("The username provided does not exist")
            .parentCode(CommonErrorSpec.VALIDATION_ERROR.getCode())
            .build();
    public final static AuthnActionSpec<IdentitySubmitActionModel> SUBMIT_USER_IDENTITY_ACTION = new AuthnActionSpec.Builder<IdentitySubmitActionModel>()
            .id("submitIdentity")
            .description("Submit the user's identity")
            .modelClass(IdentitySubmitActionModel.class)
            .error(CommonErrorSpec.VALIDATION_ERROR)
            .errorDetail(IdentitySpec.INVALID_USERNAME)
            .build();

    public final static AuthnStateSpec<IdentityStateModel> IDENTITY_STATE = new AuthnStateSpec.Builder<IdentityStateModel>()
            .status("IDENTITY")
            .description("The user's identity is required")
            .modelClass(IdentityStateModel.class)
            .action(IdentitySpec.SUBMIT_USER_IDENTITY_ACTION)
            .action(CommonActionSpec.CANCEL)
            .build();
}
