/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth2.validators;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.CacheEntry;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.ResourceScopeCacheEntry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * The OIDC Scope Validation implementation. This validates "openid" scope with authorization_code, password and
 * client_credential grant types
 */
public class OIDCScopeValidator extends OAuth2ScopeValidator {

    Log log = LogFactory.getLog(OIDCScopeValidator.class);

    @Override
    public boolean validateScope(AccessTokenDO accessTokenDO, String idTokenAllowedGrantTypes) throws IdentityOAuth2Exception {

        //Get the list of scopes associated with the access token
        String[] scopes = accessTokenDO.getScope();
        //If no scopes are associated with the token
        if (scopes != null || scopes.length > 0) {
            String granTypeValue = accessTokenDO.getGrantType();
            List<String> idTokenAllowedGrantList = new ArrayList<>();
            for (String scope : scopes) {
                if (scope.trim().equals(OAuthConstants.Scope.OPENID)) {
                    //validating the authorization_code grant type with open id scope ignoring the IdTokenAllowed element
                    // defined in the identity.xml
                    if (granTypeValue.equals(GrantType.AUTHORIZATION_CODE.toString())) {
                        return true;
                    }
                    if (StringUtils.isNotBlank(idTokenAllowedGrantTypes)) {
                        idTokenAllowedGrantList = Arrays.asList(idTokenAllowedGrantTypes.substring(1,
                                idTokenAllowedGrantTypes.length() - 1).split(", "));
                    }
                    if (!idTokenAllowedGrantList.isEmpty() && idTokenAllowedGrantList.contains(granTypeValue)) {
                        return true;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("SupportedGrantTypes or IdTokenAllowed element is not defined in the identity.xml.");
                        }
                        return false;
                    }
                }
                return true;
            }
            if (log.isDebugEnabled()) {
                log.debug("There is no any requested scope.");
            }
            return false;

        } else {
            return false;
        }
    }
}
