package identity;

import com.pingidentity.sdk.AuthnAdapterResponse;
import com.pingidentity.sdk.IdpAuthenticationAdapterV2;
import com.pingidentity.sdk.api.authn.AuthnApiPlugin;
import com.pingidentity.sdk.api.authn.common.CommonActionSpec;
import com.pingidentity.sdk.api.authn.common.CommonErrorSpec;
import com.pingidentity.sdk.api.authn.exception.AuthnErrorException;
import com.pingidentity.sdk.api.authn.model.AuthnError;
import com.pingidentity.sdk.api.authn.model.AuthnErrorDetail;
import com.pingidentity.sdk.api.authn.model.AuthnState;
import com.pingidentity.sdk.api.authn.spec.PluginApiSpec;
import com.pingidentity.sdk.api.authn.util.AuthnApiSupport;
import identity.spec.IdentitySpec;
import identity.spec.IdentityStateModel;
import identity.spec.IdentitySubmitActionModel;
import identity.spec.UsernameType;
import org.sourceid.saml20.adapter.AuthnAdapterException;
import org.sourceid.saml20.adapter.conf.Configuration;
import org.sourceid.saml20.adapter.gui.AdapterConfigurationGuiDescriptor;
import org.sourceid.saml20.adapter.gui.TextFieldDescriptor;
import org.sourceid.saml20.adapter.idp.authn.AuthnPolicy;
import org.sourceid.saml20.adapter.idp.authn.IdpAuthnAdapterDescriptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NativeIdentityAdapter implements IdpAuthenticationAdapterV2, AuthnApiPlugin {
    private final static String USERNAME_BYPASS_FIELD = "username_bypass";
    public final static String USERNAME_CONTRACT_KEY = "username";
    private final AuthnApiSupport apiSupport = AuthnApiSupport.getDefault();

    private String usernameBypass;

    @Override
    public IdpAuthnAdapterDescriptor getAdapterDescriptor() {
        AdapterConfigurationGuiDescriptor guiDescriptor = new AdapterConfigurationGuiDescriptor("Native Identity Adapter");
        guiDescriptor.addField(new TextFieldDescriptor(
                USERNAME_BYPASS_FIELD,
                "Username allowed to login"
        ));
        return new IdpAuthnAdapterDescriptor(
                this,
                "Native Identity Adapter",
                Set.of(USERNAME_CONTRACT_KEY),
                false,
                guiDescriptor,
                false
        );
    }

    @Override
    public void configure(Configuration configuration) {
        this.usernameBypass = configuration.getField(USERNAME_BYPASS_FIELD).getValue();
    }

    @Override
    public AuthnAdapterResponse lookupAuthN(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> map) throws AuthnAdapterException, IOException {
        AuthnAdapterResponse adapterResponse = new AuthnAdapterResponse();
        if(this.isCancelAction(req)) {
            adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.FAILURE);
            return adapterResponse;
        }
        if(!this.isIdentitySubmitAction(req)) {
            return this.renderIdentityState(req, resp);
        }

        IdentitySubmitActionModel identityActionModel;
        try {
            identityActionModel = this.apiSupport.deserializeAsModel(req, IdentitySubmitActionModel.class);
        } catch (AuthnErrorException e) {
            apiSupport.writeErrorResponse(req, resp, e.getValidationError());
            adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.IN_PROGRESS);
            return adapterResponse;
        }

        if(!this.usernameBypass.equals(identityActionModel.getUsername())) {
            return this.renderInvalidUsernameResponse(req, resp, identityActionModel);
        }

        adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.SUCCESS);
        adapterResponse.setAttributeMap(Map.of(USERNAME_CONTRACT_KEY, identityActionModel.getUsername()));
        return adapterResponse;
    }

    private AuthnAdapterResponse renderInvalidUsernameResponse(HttpServletRequest req, HttpServletResponse resp, IdentitySubmitActionModel identityActionModel) throws IOException {
        AuthnErrorDetail errorDetail = IdentitySpec.INVALID_USERNAME.makeInstanceBuilder()
                .message("Invalid username: " + identityActionModel.getUsername())
                .build();
        AuthnError authnError = CommonErrorSpec.VALIDATION_ERROR.makeInstance();
        authnError.setDetails(List.of(errorDetail));
        this.apiSupport.writeErrorResponse(req, resp, authnError);

        AuthnAdapterResponse adapterResponse = new AuthnAdapterResponse();
        adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.IN_PROGRESS);
        return adapterResponse;
    }

    private AuthnAdapterResponse renderIdentityState(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        IdentityStateModel identityModel = new IdentityStateModel(UsernameType.EMAIL);
        AuthnState<IdentityStateModel> authnState = this.apiSupport.makeAuthnState(req, IdentitySpec.IDENTITY_STATE, identityModel);

        this.apiSupport.writeAuthnStateResponse(req, resp, authnState);

        AuthnAdapterResponse adapterResponse = new AuthnAdapterResponse();
        adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.IN_PROGRESS);
        return adapterResponse;
    }

    private Boolean isIdentitySubmitAction(HttpServletRequest req) {
        if(!this.apiSupport.isApiRequest(req)) {
            return false;
        }
        return IdentitySpec.SUBMIT_USER_IDENTITY_ACTION.isRequested(req);
    }

    private Boolean isCancelAction(HttpServletRequest req) {
        if(!this.apiSupport.isApiRequest(req)) {
            return false;
        }
        return CommonActionSpec.CANCEL.isRequested(req);
    }

    @Override
    public PluginApiSpec getApiSpec() {
        PluginApiSpec result = new PluginApiSpec(Arrays.asList(IdentitySpec.IDENTITY_STATE));
        return result;
    }
    @Override
    public Map lookupAuthN(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String s, AuthnPolicy authnPolicy, String s1) throws AuthnAdapterException, IOException {
        return null;
    }

    @Override
    public boolean logoutAuthN(Map map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String s) throws AuthnAdapterException, IOException {
        return false;
    }

    @Override
    public Map<String, Object> getAdapterInfo() {
        return null;
    }
}
