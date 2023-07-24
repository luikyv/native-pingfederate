package password;

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
import identity.NativeIdentityAdapter;
import org.sourceid.saml20.adapter.AuthnAdapterException;
import org.sourceid.saml20.adapter.attribute.AttributeValue;
import org.sourceid.saml20.adapter.conf.Configuration;
import org.sourceid.saml20.adapter.gui.AdapterConfigurationGuiDescriptor;
import org.sourceid.saml20.adapter.gui.TextFieldDescriptor;
import org.sourceid.saml20.adapter.idp.authn.AuthnPolicy;
import org.sourceid.saml20.adapter.idp.authn.IdpAuthnAdapterDescriptor;
import password.spec.PasswordSpec;
import password.spec.PasswordStateModel;
import password.spec.PasswordSubmitActionModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NativePasswordAdapter implements IdpAuthenticationAdapterV2, AuthnApiPlugin {
    private final static String PASSWORD_BYPASS_FIELD = "password_bypass";
    public final static String USERNAME_CONTRACT_KEY = "username";
    private final AuthnApiSupport apiSupport = AuthnApiSupport.getDefault();

    private String passwordBypass;
    @Override
    public IdpAuthnAdapterDescriptor getAdapterDescriptor() {
        AdapterConfigurationGuiDescriptor guiDescriptor = new AdapterConfigurationGuiDescriptor("Native Password Adapter");
        guiDescriptor.addField(new TextFieldDescriptor(
                PASSWORD_BYPASS_FIELD,
                "Correct password"
        ));
        return new IdpAuthnAdapterDescriptor(
                this,
                "Native Password Adapter",
                Set.of(USERNAME_CONTRACT_KEY),
                false,
                guiDescriptor,
                false
        );
    }

    @Override
    public void configure(Configuration configuration) {
        this.passwordBypass = configuration.getField(PASSWORD_BYPASS_FIELD).getValue();
    }


    @Override
    public AuthnAdapterResponse lookupAuthN(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inParams) throws AuthnAdapterException, IOException {
        AuthnAdapterResponse adapterResponse = new AuthnAdapterResponse();
        if(this.isCancelAction(req)) {
            adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.FAILURE);
            return adapterResponse;
        }
        if(!this.isPasswordSubmitAction(req)) {
            return this.renderPasswordState(req, resp, inParams);
        }

        PasswordSubmitActionModel passwordActionModel;
        try {
            passwordActionModel = this.apiSupport.deserializeAsModel(req, PasswordSubmitActionModel.class);
        } catch (AuthnErrorException e) {
            apiSupport.writeErrorResponse(req, resp, e.getValidationError());
            adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.IN_PROGRESS);
            return adapterResponse;
        }

        if(!this.passwordBypass.equals(passwordActionModel.getPassword())) {
            return this.renderInvalidPasswordResponse(req, resp, passwordActionModel);
        }

        adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.SUCCESS);
        String username = ((Map<String, AttributeValue>) inParams.get(IN_PARAMETER_NAME_CHAINED_ATTRIBUTES)).get(USERNAME_CONTRACT_KEY).getValue();
        adapterResponse.setAttributeMap(Map.of(USERNAME_CONTRACT_KEY, username));
        return adapterResponse;
    }
    private Boolean isPasswordSubmitAction(HttpServletRequest req) {
        if(!this.apiSupport.isApiRequest(req)) {
            return false;
        }
        return PasswordSpec.SUBMIT_PASSWORD_ACTION.isRequested(req);
    }

    private Boolean isCancelAction(HttpServletRequest req) {
        if(!this.apiSupport.isApiRequest(req)) {
            return false;
        }
        return CommonActionSpec.CANCEL.isRequested(req);
    }

    private AuthnAdapterResponse renderPasswordState(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inParams) throws IOException {
        String username = ((Map<String, AttributeValue>) inParams.get(IN_PARAMETER_NAME_CHAINED_ATTRIBUTES)).get(USERNAME_CONTRACT_KEY).getValue();
        PasswordStateModel passwordModel = new PasswordStateModel(username);
        AuthnState<PasswordStateModel> authnState = this.apiSupport.makeAuthnState(req, PasswordSpec.PASSWORD_STATE, passwordModel);

        this.apiSupport.writeAuthnStateResponse(req, resp, authnState);

        AuthnAdapterResponse adapterResponse = new AuthnAdapterResponse();
        adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.IN_PROGRESS);
        return adapterResponse;
    }

    private AuthnAdapterResponse renderInvalidPasswordResponse(HttpServletRequest req, HttpServletResponse resp, PasswordSubmitActionModel passwordActionModel) throws IOException {
        AuthnErrorDetail errorDetail = PasswordSpec.INVALID_PASSWORD.makeInstanceBuilder()
                .message("Invalid password")
                .build();
        AuthnError authnError = CommonErrorSpec.VALIDATION_ERROR.makeInstance();
        authnError.setDetails(List.of(errorDetail));
        this.apiSupport.writeErrorResponse(req, resp, authnError);

        AuthnAdapterResponse adapterResponse = new AuthnAdapterResponse();
        adapterResponse.setAuthnStatus(AuthnAdapterResponse.AUTHN_STATUS.IN_PROGRESS);
        return adapterResponse;
    }

    @Override
    public PluginApiSpec getApiSpec() {
        PluginApiSpec result = new PluginApiSpec(Arrays.asList(PasswordSpec.PASSWORD_STATE));
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
